package com.noahparknguyen.javadictionary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "A single term entry with its definitions and source metadata")
public record TermResponse(

        @Schema(description = "Database ID", example = "1")
        Long id,

        @Schema(description = "Display name", example = "Garbage Collection")
        String name,

        @Schema(description = "URL-safe slug derived from the name", example = "garbage-collection")
        String slug,

        @Schema(description = "Plain-language, casual definition",
                example = "The JVM automatically reclaims memory occupied by objects that are no longer reachable.")
        String casualDefinition,

        @Schema(description = "Precise, technical definition",
                example = "Garbage collection is the automatic process by which the JVM identifies heap objects with no live references and reclaims their memory.")
        String formalDefinition,

        @Schema(description = "Source book title; null for manual terms", example = "Core Java Vol I", nullable = true)
        String sourceBook,

        @Schema(description = "Source chapter; null for manual terms",
                example = "Chapter 1: An Introduction to Java", nullable = true)
        String sourceChapter,

        @Schema(description = "Keyword tags associated with this entry", example = "[\"gc\", \"memory\", \"heap\"]")
        Set<String> tags,

        @Schema(description = "True when both sourceBook and sourceChapter are null", example = "false")
        boolean manual

) {
}
