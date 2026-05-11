package com.noahparknguyen.javadictionary.exception;

/**
 * Thrown when a requested resource cannot be found in the database or configuration.
 *
 * <p>Handled by {@link GlobalExceptionHandler}, which maps it to an HTTP 404 response
 * with an {@link ErrorResponse} body. Two constructors are provided: one for a generic
 * free-form message and one for the common pattern of "type not found by id."
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }
}