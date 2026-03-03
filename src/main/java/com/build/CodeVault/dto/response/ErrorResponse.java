package com.build.CodeVault.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Map;

/**
 * Detailed error response used as the {@code data} payload in
 * {@link com.build.CodeVault.dto.ApiResponse}
 * when validation or structured error information is needed.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** General error label (e.g., "Validation Failed") */
    private String error;

    /** Per-field validation errors: field name → error message */
    private Map<String, String> fieldErrors;
}
