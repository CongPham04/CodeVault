package com.build.CodeVault.service;

import com.build.CodeVault.dto.response.FileEntry;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

/**
 * Abstraction for all file system operations.
 * Implementations can be swapped (e.g., local disk → S3) without changing
 * consumers.
 */
public interface FileStorageService {

    /**
     * Creates the directory (and parents) if it does not exist.
     */
    void createDirectoryIfNotExists(Path dirPath);

    /**
     * Recursively deletes a directory and all its contents.
     */
    void deleteDirectoryRecursively(Path dirPath);

    /**
     * Safely extracts a ZIP file into the target directory with Zip Slip
     * protection.
     */
    void unzipFileSafely(MultipartFile file, Path targetDir);

    /**
     * Compresses the given directory into a temporary ZIP file and returns its
     * path.
     */
    Path zipDirectory(Path sourceDir);

    /**
     * Calculates the total size (in bytes) of all files in a directory,
     * recursively.
     */
    long calculateDirectorySize(Path dirPath);

    /**
     * Recursively lists all files and directories under the given path.
     */
    List<FileEntry> listFilesRecursively(Path dirPath);
}
