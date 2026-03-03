package com.build.CodeVault.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity mapped to the {@code repositories} table.
 * Stores only metadata — actual source code files live on the file system.
 */
@Entity
@Table(name = "repositories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 150, nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "folder_path", length = 255, nullable = false)
    private String folderPath;

    @Column(name = "size_in_bytes")
    @Builder.Default
    private Long sizeInBytes = 0L;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Set manually only when source code is uploaded — NOT auto-updated on every
     * save.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
