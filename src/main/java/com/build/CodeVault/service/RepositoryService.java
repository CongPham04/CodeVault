package com.build.CodeVault.service;

import com.build.CodeVault.dto.request.CreateRepositoryRequest;
import com.build.CodeVault.dto.response.FileEntry;
import com.build.CodeVault.dto.response.RepositoryResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Business logic interface for repository operations.
 */
public interface RepositoryService {

    RepositoryResponse createRepository(CreateRepositoryRequest request);

    List<RepositoryResponse> getAllRepositories();

    RepositoryResponse getRepositoryById(Long id);

    void deleteRepository(Long id);

    RepositoryResponse uploadSourceCode(Long id, MultipartFile file);

    Resource downloadRepository(Long id);

    List<FileEntry> listFiles(Long id);
}
