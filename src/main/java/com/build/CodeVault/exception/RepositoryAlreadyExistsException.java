package com.build.CodeVault.exception;

/**
 * Thrown when attempting to create a repository with a name that already
 * exists.
 */
public class RepositoryAlreadyExistsException extends RuntimeException {

    public RepositoryAlreadyExistsException(String name) {
        super("Repository '" + name + "' already exists");
    }
}
