package com.build.CodeVault.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Generic API response wrapper.
 * All endpoints return responses wrapped in this structure for consistency.
 *
 * @param <T> the type of the payload data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** HTTP status code (e.g., 200, 201, 404) */
    private int status;

    /** Human-readable message (e.g., "Repository created successfully") */
    private String message;

    /** Payload — generic type (RepositoryResponse, List<FileEntry>, etc.) */
    private T data;

    // ── Factory methods ──

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .status(201)
                .message("Created successfully")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .status(201)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(null)
                .build();
    }
}
