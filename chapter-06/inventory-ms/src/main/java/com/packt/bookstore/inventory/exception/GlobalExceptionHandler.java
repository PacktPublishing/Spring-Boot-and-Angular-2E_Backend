package com.packt.bookstore.inventory.exception;

import org.hibernate.HibernateException;
import org.hibernate.LazyInitializationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.packt.bookstore.inventory.dto.ApiError;

import jakarta.servlet.http.HttpServletRequest;


/**
 * Centralized exception handling for all REST controllers.
 * Keeps error responses consistent and maps exceptions to proper HTTP status codes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

     /** Resource not found → 404 Not Found */
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(404).body(
                ApiError.notFound(ex.getMessage(), req.getRequestURI()));
    }

    /** Validation failure → 400 Bad Request */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> badRequest(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.badRequest().body(
                ApiError.badRequest(detail, req.getRequestURI()));
    }

    /** Database constraint violation → 409 Conflict */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        return ResponseEntity.status(409).body(
                ApiError.conflict("Data constraint violation occurred", req.getRequestURI()));
    }

    /** Business rule violation → 422 Unprocessable Entity */
    @ExceptionHandler(DomainRuleViolationException.class)
    ResponseEntity<ApiError> unprocessable(DomainRuleViolationException ex, HttpServletRequest req) {
        return ResponseEntity.status(422).body(
                ApiError.unprocessableEntity(ex.getMessage(), req.getRequestURI())); // Changed this line
    }

    /** Lazy initialization error → 500 Internal Server Error */
    @ExceptionHandler(LazyInitializationException.class)
    ResponseEntity<ApiError> lazyInitialization(LazyInitializationException ex, HttpServletRequest req) {
        return ResponseEntity.status(500).body(
                ApiError.dataLoadingError("Failed to load related data. Please try again.", req.getRequestURI()));
    }
    
    /** Hibernate database error → 500 Internal Server Error */
    @ExceptionHandler(HibernateException.class)
    ResponseEntity<ApiError> hibernateError(HibernateException ex, HttpServletRequest req) {
        return ResponseEntity.status(500).body(
                ApiError.databaseError("A database operation failed", req.getRequestURI()));
    }

    /** Fallback for unexpected errors → 500 Internal Server Error */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(500).body(
                ApiError.internalServerError("An unexpected error occurred", req.getRequestURI()));
    }
    
}

