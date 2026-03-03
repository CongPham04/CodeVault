package com.build.CodeVault.exception;

/**
 * Thrown when a file system operation fails (create, delete, zip, unzip, etc.).
 */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
