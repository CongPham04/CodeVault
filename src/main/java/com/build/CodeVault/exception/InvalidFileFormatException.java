package com.build.CodeVault.exception;

/**
 * Thrown when an uploaded file is not a valid ZIP archive.
 */
public class InvalidFileFormatException extends RuntimeException {

    public InvalidFileFormatException(String message) {
        super(message);
    }
}
