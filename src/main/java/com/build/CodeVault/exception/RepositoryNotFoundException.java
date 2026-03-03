package com.build.CodeVault.exception;

/**
 * Thrown when a repository with the given ID does not exist.
 */
public class RepositoryNotFoundException extends RuntimeException {

    public RepositoryNotFoundException(Long id) {
        super("Repository not found with id: " + id);
    }

    public RepositoryNotFoundException(String message) {
        super(message);
    }
}
