package com.build.CodeVault.repository;

import com.build.CodeVault.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Repository} entity.
 * No {@code @Repository} annotation needed — Spring Data auto-detects JPA
 * interfaces.
 */
public interface RepositoryJpaRepository extends JpaRepository<Repository, Long> {

    Optional<Repository> findByName(String name);

    boolean existsByName(String name);
}
