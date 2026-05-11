package com.noahparknguyen.javadictionary.service;

import com.noahparknguyen.javadictionary.config.roadmap.EntryConfig;
import com.noahparknguyen.javadictionary.config.roadmap.RoadmapProperties;
import com.noahparknguyen.javadictionary.config.roadmap.VolumeConfig;
import com.noahparknguyen.javadictionary.dto.response.RoadmapChapterView;
import com.noahparknguyen.javadictionary.dto.response.RoadmapEntryView;
import com.noahparknguyen.javadictionary.exception.ResourceNotFoundException;
import com.noahparknguyen.javadictionary.repository.TermRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class RoadmapService {

    private final RoadmapProperties roadmapProperties;
    private final TermRepository termRepository;
    private final TermService termService;

    public RoadmapService(RoadmapProperties roadmapProperties,
                          TermRepository termRepository,
                          TermService termService) {
        this.roadmapProperties = roadmapProperties;
        this.termRepository = termRepository;
        this.termService = termService;
    }

    // ── Volume lookups ────────────────────────────────────────────────────────

    public List<VolumeConfig> getAllVolumes() {
        return roadmapProperties.getVolumes();
    }

    public VolumeConfig getVolume(String slug) {
        return roadmapProperties.getVolumes().stream()
                .filter(v -> v.getSlug().equals(slug))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap volume not found: " + slug));
    }

    // ── DB-enriched chapter views for a volume page ───────────────────────────

    /**
     * Builds the full chapter/entry view for a volume page.
     * Each entry is checked against the DB so the template knows whether
     * a definition already exists for this (term, book, chapter) combination.
     */
    @Transactional(readOnly = true)
    public List<RoadmapChapterView> getChapterViews(String volumeSlug) {
        VolumeConfig volume = getVolume(volumeSlug);
        String sourceBook = volume.getBook();

        return volume.getChapters().stream()
                .map(chapter -> new RoadmapChapterView(
                        chapter.getName(),
                        chapter.getDescription(),
                        chapter.getEntries().stream()
                                .map(entry -> toEntryView(entry, sourceBook, chapter.getName()))
                                .toList()
                ))
                .toList();
    }

    private RoadmapEntryView toEntryView(EntryConfig entry, String sourceBook, String sourceChapter) {
        return termRepository.findByNameAndSource(entry.getTerm(), sourceBook, sourceChapter)
                .map(term -> RoadmapEntryView.of(entry, true, term.getId()))
                .orElseGet(() -> RoadmapEntryView.of(entry, false, null));
    }

    // ── Submit a definition from the roadmap form ─────────────────────────────

    /**
     * Creates or overwrites the book-sourced term for a roadmap entry.
     * Delegates to {@link TermService#saveBookTerm} which handles the
     * duplicate/override logic and throws {@link IllegalStateException}
     * when a definition already exists and override is false.
     */
    @Transactional
    public void submitEntry(String volumeSlug,
                            String chapterName,
                            EntryConfig entry,
                            String casualDefinition,
                            String formalDefinition,
                            boolean override) {

        VolumeConfig volume = getVolume(volumeSlug);
        String sourceBook = volume.getBook();

        log.info("Roadmap submit — term: '{}', book: '{}', chapter: '{}', override: {}",
                entry.getTerm(), sourceBook, chapterName, override);

        termService.saveBookTerm(
                entry.getTerm(),
                casualDefinition,
                formalDefinition,
                sourceBook,
                chapterName,
                entry.getTags() != null ? new HashSet<>(entry.getTags()) : new HashSet<>(),
                override
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Finds the EntryConfig for a given term name within a specific chapter of a volume.
     * Used by the controller to reconstruct the entry after a form POST.
     */
    public EntryConfig findEntry(String volumeSlug, String chapterName, String termName) {
        VolumeConfig volume = getVolume(volumeSlug);
        return volume.getChapters().stream()
                .filter(c -> c.getName().equals(chapterName))
                .flatMap(c -> c.getEntries().stream())
                .filter(e -> e.getTerm().equalsIgnoreCase(termName))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No roadmap entry found for term '" + termName
                                + "' in chapter '" + chapterName + "'"));
    }

}
