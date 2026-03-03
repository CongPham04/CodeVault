package com.build.CodeVault.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request body for creating a new repository.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRepositoryRequest {

    @NotBlank(message = "Repository name is required")
    @Size(max = 150, message = "Repository name must not exceed 150 characters")
    private String name;

    private String description;
}
