package com.noahparknguyen.javadictionary.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Standard error body returned for all 4xx and 5xx responses")
public record ErrorResponse(

        @Schema(description = "HTTP status code", example = "404")
        int status,

        @Schema(description = "Human-readable error message", example = "Term not found with id: 99")
        String message,

        @Schema(description = "Per-field validation errors; present only for 400 validation failures, null otherwise",
                nullable = true, example = "{\"name\": \"Name is required\"}")
        Map<String, String> errors,

        @Schema(description = "Server timestamp when the error occurred")
        LocalDateTime timestamp

) {
}
