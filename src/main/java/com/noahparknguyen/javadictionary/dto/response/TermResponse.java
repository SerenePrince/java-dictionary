package com.noahparknguyen.javadictionary.dto.response;

import java.util.Set;

public record TermResponse(
        Long id,
        String name,
        String slug,
        String casualDefinition,
        String formalDefinition,
        String sourceBook,
        String sourceChapter,
        Set<String> tags,
        boolean manual
) {
}
