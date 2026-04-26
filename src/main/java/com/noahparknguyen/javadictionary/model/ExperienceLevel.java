package com.noahparknguyen.javadictionary.model;

import lombok.Getter;

@Getter
public enum ExperienceLevel {
    ENTRY("Entry"),
    JUNIOR("Junior"),
    INTERMEDIATE("Intermediate"),
    SENIOR("Senior");

    private final String displayName;

    ExperienceLevel(String displayName) {
        this.displayName = displayName;
    }
}
