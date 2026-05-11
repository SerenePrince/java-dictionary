package com.noahparknguyen.javadictionary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Request body for creating or updating a term entry")
public class CreateTermRequest {

    @Schema(description = "Display name of the term", example = "Garbage Collection", maxLength = 100)
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be under 100 characters")
    private String name;

    @Schema(description = "Plain-language explanation suitable for casual conversation",
            example = "The JVM automatically reclaims memory occupied by objects that are no longer reachable, so you don't have to free it manually.",
            maxLength = 500)
    @NotBlank(message = "Casual definition is required")
    @Size(max = 500, message = "Casual definition must be under 500 characters")
    private String casualDefinition;

    @Schema(description = "Precise, interview-ready technical definition",
            example = "Garbage collection is the automatic process by which the JVM identifies heap objects with no live references and reclaims their memory.",
            maxLength = 1000)
    @NotBlank(message = "Formal definition is required")
    @Size(max = 1000, message = "Formal definition must be under 1000 characters")
    private String formalDefinition;

    @Schema(description = "Set of keyword tags (max 10, each must be non-blank)",
            example = "[\"gc\", \"memory\", \"heap\"]")
    @Size(max = 10, message = "You can have at most 10 tags")
    private Set<@NotBlank String> tags = new HashSet<>();
}
