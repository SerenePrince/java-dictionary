package com.noahparknguyen.javadictionary.dto.response;

import com.noahparknguyen.javadictionary.model.ExperienceLevel;

import java.util.Map;

public record TermResponse(
        Long id,
        String name,
        Map<ExperienceLevel, TermDefinitionResponse> definitions
) {
}
