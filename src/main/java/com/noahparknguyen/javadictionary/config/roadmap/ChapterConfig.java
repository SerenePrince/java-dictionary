package com.noahparknguyen.javadictionary.config.roadmap;

import java.util.ArrayList;
import java.util.List;

/**
 * A Chapter is a subtopic within a Volume (e.g. "Primitive Types", "Inheritance").
 * Equivalent to what might be called a "section" in other contexts.
 */
public class ChapterConfig {

    /** Display name shown as the chapter heading. */
    private String name;

    /** Optional short description shown under the chapter heading. */
    private String description;

    /** The dictionary entries (terms) to learn in this chapter, in order. */
    private List<EntryConfig> entries = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<EntryConfig> getEntries() { return entries; }
    public void setEntries(List<EntryConfig> entries) { this.entries = entries; }
}
