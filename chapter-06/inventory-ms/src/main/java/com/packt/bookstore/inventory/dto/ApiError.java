package com.packt.bookstore.inventory.dto;

import java.time.Instant;

/**
 * A structured error response following Problem-Details style (RFC 7807).
 * Every error response in the API will be serialized into this shape.
 */
public record ApiError(
        String type,      // A URI identifier for the error type
        String title,     // Short summary
        int status,       // HTTP status code
        String detail,    // Human-readable details
        String instance,  // The request path
        Instant timestamp // When the error occurred
) {
    // Base URI for your API error documentation
     private static final String ERROR_TYPE_BASE = "https://api.bookstore.com/errors/";

    public static ApiError notFound(String detail, String instance) {
        return new ApiError(ERROR_TYPE_BASE + "not-found", "Not Found", 404, detail, instance, Instant.now());
    }
    
    public static ApiError badRequest(String detail, String instance) {
        return new ApiError(ERROR_TYPE_BASE + "bad-request", "Bad Request", 400, detail, instance, Instant.now());
    }
    
    public static ApiError conflict(String detail, String instance) {
        return new ApiError(ERROR_TYPE_BASE + "conflict", "Conflict", 409, detail, instance, Instant.now());
    }
    
    public static ApiError unprocessableEntity(String detail, String instance) {
        return new ApiError(ERROR_TYPE_BASE + "unprocessable-entity", "Unprocessable Entity", 422, detail, instance, Instant.now());
    }
    
    public static ApiError dataLoadingError(String detail, String instance) {
        return new ApiError(ERROR_TYPE_BASE + "data-loading-error", "Data Loading Error", 500, detail, instance, Instant.now());
    }
    
    public static ApiError databaseError(String detail, String instance) {
        return new ApiError(ERROR_TYPE_BASE + "database-error", "Database Error", 500, detail, instance, Instant.now());
    }
    
    public static ApiError internalServerError(String detail, String instance) {
        return new ApiError(ERROR_TYPE_BASE + "internal-server-error", "Internal Server Error", 500, detail, instance, Instant.now());
    }
}