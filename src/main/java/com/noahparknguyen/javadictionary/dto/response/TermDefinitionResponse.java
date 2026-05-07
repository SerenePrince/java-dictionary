package com.noahparknguyen.javadictionary.dto.response;

import com.noahparknguyen.javadictionary.model.ExperienceLevel;

import java.util.Set;

public record TermDefinitionResponse(
        Long id,
        ExperienceLevel experienceLevel,
        String casualDefinition,
        String formalDefinition,
        Set<String> tags
) {
}
