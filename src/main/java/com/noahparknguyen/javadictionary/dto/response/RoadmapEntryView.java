package com.noahparknguyen.javadictionary.dto.response;

import com.noahparknguyen.javadictionary.config.roadmap.EntryConfig;
import com.noahparknguyen.javadictionary.config.roadmap.HintConfig;
import com.noahparknguyen.javadictionary.config.roadmap.ResourceConfig;

import java.util.List;
import java.util.Set;

/**
 * A read model combining a roadmap EntryConfig with its current DB state.
 * Passed to the Thymeleaf template so it can render the accordion and form
 * without needing to query anything itself.
 */
public record RoadmapEntryView(

        /** The configured term name. */
        String term,

        /** Pre-suggested tags from config. */
        Set<String> tags,

        /** External learning resources. */
        List<ResourceConfig> resources,

        /** Research guidance prompts. */
        HintConfig hints,

        /** True if a definition already exists in the DB for this (term, book, chapter). */
        boolean alreadySaved,

        /** DB term id, non-null only when alreadySaved == true. */
        Long existingTermId

) {
    public static RoadmapEntryView of(EntryConfig config, boolean alreadySaved, Long existingTermId) {
        return new RoadmapEntryView(
                config.getTerm(),
                config.getTags(),
                config.getResources(),
                config.getHints(),
                alreadySaved,
                existingTermId
        );
    }
}
