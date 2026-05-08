package com.noahparknguyen.javadictionary.service;

import com.noahparknguyen.javadictionary.config.roadmap.ChapterConfig;
import com.noahparknguyen.javadictionary.config.roadmap.EntryConfig;
import com.noahparknguyen.javadictionary.config.roadmap.RoadmapProperties;
import com.noahparknguyen.javadictionary.config.roadmap.VolumeConfig;
import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.request.UpdateTermRequest;
import com.noahparknguyen.javadictionary.dto.response.RoadmapChapterView;
import com.noahparknguyen.javadictionary.dto.response.RoadmapEntryView;
import com.noahparknguyen.javadictionary.exception.ResourceNotFoundException;
import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import com.noahparknguyen.javadictionary.model.Term;
import com.noahparknguyen.javadictionary.repository.TermDefinitionRepository;
import com.noahparknguyen.javadictionary.repository.TermRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class RoadmapService {

    private final RoadmapProperties roadmapProperties;
    private final TermRepository termRepository;
    private final TermDefinitionRepository termDefinitionRepository;
    private final TermService termService;

    public RoadmapService(RoadmapProperties roadmapProperties,
                          TermRepository termRepository,
                          TermDefinitionRepository termDefinitionRepository,
                          TermService termService) {
        this.roadmapProperties = roadmapProperties;
        this.termRepository = termRepository;
        this.termDefinitionRepository = termDefinitionRepository;
        this.termService = termService;
    }

    // -------------------------------------------------------------------------
    // Volume lookups
    // -------------------------------------------------------------------------

    public List<VolumeConfig> getAllVolumes() {
        return roadmapProperties.getVolumes();
    }

    public VolumeConfig getVolume(String slug) {
        return roadmapProperties.getVolumes().stream()
                .filter(v -> v.getSlug().equals(slug))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap volume not found: " + slug));
    }

    // -------------------------------------------------------------------------
    // DB-enriched chapter views for a volume page
    // -------------------------------------------------------------------------

    /**
     * Builds the full chapter/entry view for a volume page.
     * Each entry is checked against the DB so the template knows whether
     * a definition at that level already exists.
     */
    @Transactional(readOnly = true)
    public List<RoadmapChapterView> getChapterViews(String volumeSlug) {
        VolumeConfig volume = getVolume(volumeSlug);

        return volume.getChapters().stream()
                .map(chapter -> new RoadmapChapterView(
                        chapter.getName(),
                        chapter.getDescription(),
                        chapter.getEntries().stream()
                                .map(this::toEntryView)
                                .toList()
                ))
                .toList();
    }

    private RoadmapEntryView toEntryView(EntryConfig entry) {
        Optional<Term> existing = termRepository.findByNameIgnoreCase(entry.getTerm());
        if (existing.isEmpty()) {
            return RoadmapEntryView.of(entry, false, null);
        }
        Term term = existing.get();
        boolean definitionExists = termDefinitionRepository
                .existsByTermIdAndExperienceLevel(term.getId(), entry.getExperienceLevel());
        return RoadmapEntryView.of(entry, definitionExists, term.getId());
    }

    // -------------------------------------------------------------------------
    // Submit a definition from the roadmap form
    // -------------------------------------------------------------------------

    /**
     * Creates or overwrites the definition for a roadmap entry.
     *
     * <ul>
     *   <li>If the term doesn't exist yet → creates a new Term + TermDefinition.</li>
     *   <li>If the term exists but has no definition at this level → adds the definition.</li>
     *   <li>If a definition already exists at this level and {@code override} is true →
     *       overwrites it.</li>
     *   <li>If a definition already exists and {@code override} is false → throws
     *       {@link IllegalStateException} so the controller can ask for confirmation.</li>
     * </ul>
     */
    @Transactional
    public void submitEntry(EntryConfig entry,
                            String casualDefinition,
                            String formalDefinition,
                            boolean override) {

        log.info("Roadmap submit — term: '{}', level: {}, override: {}",
                entry.getTerm(), entry.getExperienceLevel(), override);

        Optional<Term> existingTerm = termRepository.findByNameIgnoreCase(entry.getTerm());

        if (existingTerm.isEmpty()) {
            // Brand-new term — create it with the first definition
            CreateTermRequest request = buildCreateRequest(entry, casualDefinition, formalDefinition);
            termService.createTerm(request);
        } else {
            Term term = existingTerm.get();
            boolean definitionExists = termDefinitionRepository
                    .existsByTermIdAndExperienceLevel(term.getId(), entry.getExperienceLevel());

            if (definitionExists && !override) {
                throw new IllegalStateException("A definition already exists for '"
                        + entry.getTerm() + "' at the "
                        + entry.getExperienceLevel().getDisplayName() + " level. "
                        + "Submit again to override it.");
            }

            // Upsert the definition (creates if missing, updates if override=true)
            UpdateTermRequest request = buildUpdateRequest(entry, term, casualDefinition, formalDefinition);
            termService.saveDefinition(term.getId(), request);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CreateTermRequest buildCreateRequest(EntryConfig entry,
                                                 String casualDefinition,
                                                 String formalDefinition) {
        CreateTermRequest request = new CreateTermRequest();
        request.setName(entry.getTerm());
        request.setExperienceLevel(entry.getExperienceLevel());
        request.setCasualDefinition(casualDefinition);
        request.setFormalDefinition(formalDefinition);
        request.setTags(new HashSet<>(entry.getTags()));
        return request;
    }

    private UpdateTermRequest buildUpdateRequest(EntryConfig entry,
                                                 Term term,
                                                 String casualDefinition,
                                                 String formalDefinition) {
        UpdateTermRequest request = new UpdateTermRequest();
        request.setName(term.getName()); // preserve existing name casing
        request.setExperienceLevel(entry.getExperienceLevel());
        request.setCasualDefinition(casualDefinition);
        request.setFormalDefinition(formalDefinition);
        request.setTags(new HashSet<>(entry.getTags()));
        return request;
    }

    /**
     * Finds the EntryConfig for a given term name and experience level within a volume.
     * Used by the controller to reconstruct the entry after a form POST.
     */
    public EntryConfig findEntry(String volumeSlug, String termName, ExperienceLevel level) {
        VolumeConfig volume = getVolume(volumeSlug);
        return volume.getChapters().stream()
                .flatMap(chapter -> chapter.getEntries().stream())
                .filter(e -> e.getTerm().equalsIgnoreCase(termName)
                        && e.getExperienceLevel() == level)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No roadmap entry found for term '" + termName + "' at level " + level));
    }
}
