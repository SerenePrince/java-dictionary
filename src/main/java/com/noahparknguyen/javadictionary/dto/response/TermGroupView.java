package com.noahparknguyen.javadictionary.dto.response;

import java.util.List;

/**
 * Aggregates all Term entries that share the same name/slug.
 * Used by the index page (one card per group) and the group detail page
 * (one accordion row per entry).
 */
public record TermGroupView(
        String name,
        String slug,
        List<TermResponse> entries
) {
}
