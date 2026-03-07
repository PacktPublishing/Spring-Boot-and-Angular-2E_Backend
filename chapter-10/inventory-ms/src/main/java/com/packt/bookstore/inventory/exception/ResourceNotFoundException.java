package com.packt.bookstore.inventory.exception;

/**
 * Thrown when a requested resource (e.g., Book, Author) does not exist.
 * Mapped to HTTP 404 Not Found by the global exception handler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
