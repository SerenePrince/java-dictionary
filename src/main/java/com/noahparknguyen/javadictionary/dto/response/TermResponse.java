package com.noahparknguyen.javadictionary.dto.response;

import com.noahparknguyen.javadictionary.model.ExperienceLevel;

import java.util.Set;

public record TermResponse(
        Long id,
        String name,
        String casualDefinition,
        String formalDefinition,
        ExperienceLevel experienceLevel,
        Set<String> tags
) {
}