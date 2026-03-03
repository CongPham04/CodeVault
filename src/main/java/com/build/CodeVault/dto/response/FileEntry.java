package com.build.CodeVault.dto.response;

import lombok.*;

/**
 * Represents a single file or directory entry in a repository listing.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntry {

    /** File or directory name (e.g., "pom.xml", "src") */
    private String name;

    /** Entry type: "file" or "directory" */
    private String type;

    /** Relative path from the repository root (e.g., "src/main/java/App.java") */
    private String path;
}
