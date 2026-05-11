package com.noahparknguyen.javadictionary.exception;

/**
 * Thrown when an attempt is made to create or rename a resource with a name that
 * already exists under the same uniqueness scope.
 *
 * <p>For manual terms, the uniqueness scope is the term name alone (case-insensitive).
 * Handled by {@link GlobalExceptionHandler}, which maps it to an HTTP 409 Conflict response.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}