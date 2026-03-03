package com.build.CodeVault.service;

import com.build.CodeVault.config.StorageConfig;
import com.build.CodeVault.dto.request.CreateRepositoryRequest;
import com.build.CodeVault.dto.response.FileEntry;
import com.build.CodeVault.dto.response.RepositoryResponse;
import com.build.CodeVault.entity.Repository;
import com.build.CodeVault.exception.RepositoryAlreadyExistsException;
import com.build.CodeVault.exception.RepositoryNotFoundException;
import com.build.CodeVault.repository.RepositoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link RepositoryService}.
 * Orchestrates between JPA repository and file storage operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryServiceImpl implements RepositoryService {

    private final RepositoryJpaRepository jpaRepository;
    private final FileStorageService fileStorageService;
    private final StorageConfig storageConfig;

    @Override
    @Transactional
    public RepositoryResponse createRepository(CreateRepositoryRequest request) {
        // Validate unique name
        if (jpaRepository.existsByName(request.getName())) {
            throw new RepositoryAlreadyExistsException(request.getName());
        }

        // Save entity to get auto-generated ID
        Repository repo = Repository.builder()
                .name(request.getName())
                .description(request.getDescription())
                .folderPath("pending") // temporary — updated after we get the ID
                .build();

        repo = jpaRepository.save(repo);

        // Build real folder path using the generated ID
        Path folderPath = Paths.get(storageConfig.getLocation(), "repos", String.valueOf(repo.getId()));
        repo.setFolderPath(folderPath.toString());
        repo = jpaRepository.save(repo);

        // Create physical directory
        fileStorageService.createDirectoryIfNotExists(folderPath);

        log.info("Created repository '{}' with id={}", repo.getName(), repo.getId());
        return RepositoryResponse.fromEntity(repo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepositoryResponse> getAllRepositories() {
        return jpaRepository.findAll()
                .stream()
                .map(RepositoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryResponse getRepositoryById(Long id) {
        Repository repo = findRepositoryOrThrow(id);
        return RepositoryResponse.fromEntity(repo);
    }

    @Override
    @Transactional
    public void deleteRepository(Long id) {
        Repository repo = findRepositoryOrThrow(id);

        // Delete physical folder first
        fileStorageService.deleteDirectoryRecursively(Paths.get(repo.getFolderPath()));

        // Then delete database record
        jpaRepository.delete(repo);
        log.info("Deleted repository '{}' with id={}", repo.getName(), id);
    }

    @Override
    @Transactional
    public RepositoryResponse uploadSourceCode(Long id, MultipartFile file) {
        Repository repo = findRepositoryOrThrow(id);
        Path repoPath = Paths.get(repo.getFolderPath());

        // Clear existing content
        fileStorageService.deleteDirectoryRecursively(repoPath);

        // Extract uploaded ZIP safely
        fileStorageService.unzipFileSafely(file, repoPath);

        // Update metadata
        long totalSize = fileStorageService.calculateDirectorySize(repoPath);
        repo.setSizeInBytes(totalSize);
        repo.setUpdatedAt(LocalDateTime.now());
        repo = jpaRepository.save(repo);

        log.info("Uploaded source code to repository '{}' (size: {} bytes)", repo.getName(), totalSize);
        return RepositoryResponse.fromEntity(repo);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadRepository(Long id) {
        Repository repo = findRepositoryOrThrow(id);
        Path repoPath = Paths.get(repo.getFolderPath());

        // Create temp ZIP
        Path tempZip = fileStorageService.zipDirectory(repoPath);

        try {
            byte[] zipBytes = Files.readAllBytes(tempZip);
            Files.deleteIfExists(tempZip);
            return new ByteArrayResource(zipBytes);
        } catch (IOException e) {
            throw new com.build.CodeVault.exception.StorageException(
                    "Failed to read ZIP archive for repository: " + repo.getName(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileEntry> listFiles(Long id) {
        Repository repo = findRepositoryOrThrow(id);
        Path repoPath = Paths.get(repo.getFolderPath());
        return fileStorageService.listFilesRecursively(repoPath);
    }

    // ── Private helpers ──

    private Repository findRepositoryOrThrow(Long id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new RepositoryNotFoundException(id));
    }
}
