package com.noahparknguyen.javadictionary.dto.response;

import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class TermResponse {
    private Long id;
    private String name;
    private String casualDefinition;
    private String formalDefinition;
    private ExperienceLevel experienceLevel;
    private Set<String> tags;
}