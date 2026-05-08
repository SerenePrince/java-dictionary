package com.noahparknguyen.javadictionary.config.roadmap;

import com.noahparknguyen.javadictionary.model.ExperienceLevel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A single dictionary entry within a Chapter.
 * Specifies the term to define, which experience level to target,
 * pre-suggested tags, learning resources, and research hints.
 */
public class EntryConfig {

    /** The exact term name to create/update in the dictionary. */
    private String term;

    /**
     * The experience level at which the user should write their definition.
     * The same term can appear in multiple chapters at different levels
     * (e.g. "String" at ENTRY in Foundations, JUNIOR in Advanced Java).
     */
    private ExperienceLevel experienceLevel = ExperienceLevel.ENTRY;

    /** Pre-suggested tags that will be pre-filled in the submission form. */
    private Set<String> tags = new LinkedHashSet<>();

    /** External links (articles, docs, videos) to help the user research this term. */
    private List<ResourceConfig> resources = new ArrayList<>();

    /** Guided research prompts to direct the user's learning. */
    private HintConfig hints;

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(ExperienceLevel experienceLevel) { this.experienceLevel = experienceLevel; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }

    public List<ResourceConfig> getResources() { return resources; }
    public void setResources(List<ResourceConfig> resources) { this.resources = resources; }

    public HintConfig getHints() { return hints; }
    public void setHints(HintConfig hints) { this.hints = hints; }
}
