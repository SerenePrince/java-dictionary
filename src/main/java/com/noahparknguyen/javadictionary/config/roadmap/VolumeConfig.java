package com.noahparknguyen.javadictionary.config.roadmap;

import java.util.ArrayList;
import java.util.List;

/**
 * A Volume maps to a specific book in the roadmap (e.g. "Core Java Vol I").
 * Each volume has a slug for routing, a display name, an optional description,
 * the book title used as the sourceBook on all saved terms, and its chapters.
 */
public class VolumeConfig {

    /** URL-safe identifier used in route paths (e.g. "core-java-vol-1"). */
    private String slug;

    /** Display name shown in the UI (e.g. "Core Java Vol I"). */
    private String name;

    /** Short description shown on the roadmap index page. */
    private String description;

    /**
     * The book title stored as sourceBook on every term saved through this volume.
     * Must match exactly the value used in filter queries.
     */
    private String book;

    private List<ChapterConfig> chapters = new ArrayList<>();

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBook() { return book; }
    public void setBook(String book) { this.book = book; }

    public List<ChapterConfig> getChapters() { return chapters; }
    public void setChapters(List<ChapterConfig> chapters) { this.chapters = chapters; }
}
