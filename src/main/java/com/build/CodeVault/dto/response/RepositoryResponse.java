package com.build.CodeVault.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for repository metadata.
 * Decouples the API contract from the JPA entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryResponse {

    private Long id;
    private String name;
    private String description;
    private String folderPath;
    private Long sizeInBytes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Maps a JPA entity to a response DTO.
     */
    public static RepositoryResponse fromEntity(com.build.CodeVault.entity.Repository entity) {
        return RepositoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .folderPath(entity.getFolderPath())
                .sizeInBytes(entity.getSizeInBytes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
