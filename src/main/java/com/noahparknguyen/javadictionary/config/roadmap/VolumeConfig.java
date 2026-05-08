package com.noahparknguyen.javadictionary.config.roadmap;

import java.util.ArrayList;
import java.util.List;

/**
 * A Volume is a major topic area in the roadmap (e.g. "Java Foundations", "OOP").
 * Equivalent to what might be called a "course" in other contexts.
 */
public class VolumeConfig {

    /** URL-safe identifier used in route paths (e.g. "java-foundations"). */
    private String slug;

    /** Display name shown in the UI (e.g. "Java Foundations"). */
    private String name;

    /** Short description shown on the roadmap index page. */
    private String description;

    private List<ChapterConfig> chapters = new ArrayList<>();

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ChapterConfig> getChapters() { return chapters; }
    public void setChapters(List<ChapterConfig> chapters) { this.chapters = chapters; }
}
