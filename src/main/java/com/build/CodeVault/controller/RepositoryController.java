package com.build.CodeVault.controller;

import com.build.CodeVault.dto.ApiResponse;
import com.build.CodeVault.dto.request.CreateRepositoryRequest;
import com.build.CodeVault.dto.response.FileEntry;
import com.build.CodeVault.dto.response.RepositoryResponse;
import com.build.CodeVault.service.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for repository management:
 * CRUD operations, source code upload/download, and file listing.
 */
@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
@Tag(name = "Repository", description = "Repository management — CRUD, upload, download, file listing")
public class RepositoryController {

    private final RepositoryService repositoryService;

    // ── Create ──

    @Operation(summary = "Create a new repository", description = "Creates a new repository with a unique name. "
            + "A physical folder is auto-created on the file system.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Repository created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Repository name already exists")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<RepositoryResponse>> createRepository(
            @Valid @RequestBody CreateRepositoryRequest request) {

        RepositoryResponse response = repositoryService.createRepository(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Repository created successfully", response));
    }

    // ── List All ──

    @Operation(summary = "List all repositories", description = "Returns metadata for every repository.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RepositoryResponse>>> getAllRepositories() {
        List<RepositoryResponse> repositories = repositoryService.getAllRepositories();
        return ResponseEntity.ok(ApiResponse.success(repositories));
    }

    // ── Get by ID ──

    @Operation(summary = "Get repository details", description = "Returns metadata for a single repository by ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Repository found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Repository not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RepositoryResponse>> getRepositoryById(
            @Parameter(description = "Repository ID") @PathVariable Long id) {

        RepositoryResponse response = repositoryService.getRepositoryById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── Delete ──

    @Operation(summary = "Delete a repository", description = "Deletes the DB record and physical folder.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Repository deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Repository not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRepository(
            @Parameter(description = "Repository ID") @PathVariable Long id) {

        repositoryService.deleteRepository(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    // ── Upload ──

    @Operation(summary = "Upload source code (ZIP)", description = "Replaces existing content with the uploaded ZIP archive. Only .zip files are accepted.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Source code uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file format (not ZIP)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Repository not found")
    })
    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RepositoryResponse>> uploadSourceCode(
            @Parameter(description = "Repository ID") @PathVariable Long id,
            @Parameter(description = "ZIP file containing source code") @RequestParam("file") MultipartFile file) {

        RepositoryResponse response = repositoryService.uploadSourceCode(id, file);
        return ResponseEntity.ok(ApiResponse.success("Source code uploaded successfully", response));
    }

    // ── Download ──

    @Operation(summary = "Download repository as ZIP", description = "Streams the repository content as a ZIP archive.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "ZIP archive streamed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Repository not found")
    })
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadRepository(
            @Parameter(description = "Repository ID") @PathVariable Long id) {

        RepositoryResponse repoInfo = repositoryService.getRepositoryById(id);
        Resource resource = repositoryService.downloadRepository(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + repoInfo.getName() + ".zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(resource);
    }

    // ── List Files ──

    @Operation(summary = "List files in repository", description = "Recursively lists all files and directories.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File list returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Repository not found")
    })
    @GetMapping("/{id}/files")
    public ResponseEntity<ApiResponse<List<FileEntry>>> listFiles(
            @Parameter(description = "Repository ID") @PathVariable Long id) {

        List<FileEntry> files = repositoryService.listFiles(id);
        return ResponseEntity.ok(ApiResponse.success(files));
    }
}
