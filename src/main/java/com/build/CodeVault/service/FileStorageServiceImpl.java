package com.build.CodeVault.service;

import com.build.CodeVault.dto.response.FileEntry;
import com.build.CodeVault.exception.InvalidFileFormatException;
import com.build.CodeVault.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Local file system implementation of {@link FileStorageService}.
 */
@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Override
    public void createDirectoryIfNotExists(Path dirPath) {
        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.info("Created directory: {}", dirPath);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to create directory: " + dirPath, e);
        }
    }

    @Override
    public void deleteDirectoryRecursively(Path dirPath) {
        try {
            if (Files.exists(dirPath)) {
                FileUtils.deleteDirectory(dirPath.toFile());
                log.info("Deleted directory: {}", dirPath);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to delete directory: " + dirPath, e);
        }
    }

    @Override
    public void unzipFileSafely(MultipartFile file, Path targetDir) {
        // Validate ZIP format
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new InvalidFileFormatException("Only .zip files are accepted. Received: " + originalFilename);
        }

        createDirectoryIfNotExists(targetDir);
        Path normalizedTarget = targetDir.normalize();

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path resolvedPath = normalizedTarget.resolve(entry.getName()).normalize();

                // ── Zip Slip protection ──
                if (!resolvedPath.startsWith(normalizedTarget)) {
                    throw new StorageException(
                            "Zip Slip detected: entry '" + entry.getName() + "' escapes target directory");
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    // Ensure parent directories exist
                    Files.createDirectories(resolvedPath.getParent());
                    try (OutputStream os = Files.newOutputStream(resolvedPath)) {
                        zis.transferTo(os);
                    }
                }
                zis.closeEntry();
            }
            log.info("Extracted ZIP to: {}", targetDir);
        } catch (StorageException e) {
            throw e; // re-throw our own exceptions
        } catch (IOException e) {
            throw new StorageException("Failed to extract ZIP file", e);
        }
    }

    @Override
    public Path zipDirectory(Path sourceDir) {
        if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
            throw new StorageException("Source directory does not exist: " + sourceDir);
        }

        try {
            Path tempZip = Files.createTempFile("codevault-download-", ".zip");

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip.toFile()))) {
                Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String entryName = sourceDir.relativize(file).toString().replace("\\", "/");
                        zos.putNextEntry(new ZipEntry(entryName));
                        Files.copy(file, zos);
                        zos.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (!dir.equals(sourceDir)) {
                            String entryName = sourceDir.relativize(dir).toString().replace("\\", "/") + "/";
                            zos.putNextEntry(new ZipEntry(entryName));
                            zos.closeEntry();
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            log.info("Created ZIP archive: {}", tempZip);
            return tempZip;
        } catch (IOException e) {
            throw new StorageException("Failed to create ZIP archive from: " + sourceDir, e);
        }
    }

    @Override
    public long calculateDirectorySize(Path dirPath) {
        if (!Files.exists(dirPath)) {
            return 0L;
        }
        try {
            return FileUtils.sizeOfDirectory(dirPath.toFile());
        } catch (Exception e) {
            throw new StorageException("Failed to calculate directory size: " + dirPath, e);
        }
    }

    @Override
    public List<FileEntry> listFilesRecursively(Path dirPath) {
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            return new ArrayList<>();
        }

        List<FileEntry> entries = new ArrayList<>();
        try {
            Files.walkFileTree(dirPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    entries.add(buildFileEntry(dirPath, file, "file"));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!dir.equals(dirPath)) {
                        entries.add(buildFileEntry(dirPath, dir, "directory"));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new StorageException("Failed to list files in: " + dirPath, e);
        }

        entries.sort(Comparator.comparing(FileEntry::getPath));
        return entries;
    }

    /**
     * Builds a {@link FileEntry} with the path relative to the repo root.
     */
    private FileEntry buildFileEntry(Path root, Path entry, String type) {
        String relativePath = root.relativize(entry).toString().replace("\\", "/");
        return FileEntry.builder()
                .name(entry.getFileName().toString())
                .type(type)
                .path(relativePath)
                .build();
    }
}
