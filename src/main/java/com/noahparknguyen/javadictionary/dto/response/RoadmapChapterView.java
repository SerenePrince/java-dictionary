package com.noahparknguyen.javadictionary.dto.response;

import java.util.List;

/**
 * A read model for a single roadmap chapter, combining config metadata
 * with DB-enriched entry views.
 */
public record RoadmapChapterView(
        String name,
        String description,
        List<RoadmapEntryView> entries
) {}
