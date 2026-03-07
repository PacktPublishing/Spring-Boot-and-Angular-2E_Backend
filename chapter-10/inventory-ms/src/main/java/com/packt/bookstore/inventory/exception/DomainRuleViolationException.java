package com.packt.bookstore.inventory.exception;

/**
 * Thrown when a business rule is violated, even if the request is syntactically valid.
 * Example: attempting to set a book's price to a negative value.
 * Mapped to HTTP 422 Unprocessable Entity.
 */
public class DomainRuleViolationException extends RuntimeException {
    public DomainRuleViolationException(String message) {
        super(message);
    }
}