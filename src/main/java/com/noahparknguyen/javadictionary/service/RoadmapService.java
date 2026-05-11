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

/**
 * Provides read and write operations that bridge the static roadmap configuration
 * with live term data in the database.
 *
 * <p>The roadmap itself is entirely config-driven: volumes, chapters, and entries are
 * defined in {@code roadmap.yaml} and loaded at startup via {@link RoadmapProperties}.
 * This service enriches that static structure with per-entry DB state (does a definition
 * exist yet?) and delegates actual term persistence to {@link TermService}.
 *
 * <p><b>Key design decision — per-entry DB checks:</b> {@link #getChapterViews} issues
 * one DB query per roadmap entry to check whether a definition exists. This is intentional:
 * a single bulk query would require either an {@code IN} clause over term names or a join
 * that is hard to express in JPQL while also filtering by book/chapter. At roadmap scale
 * (tens of entries per chapter) the N+1 pattern is negligible and keeps the query logic
 * simple and readable.
 *
 * <p>{@code sourceBook} is resolved from the volume's {@code book} field at submit time,
 * not stored in the form. This avoids exposing a free-text book name as a hidden input
 * (which callers could tamper with) and keeps the config as the single source of truth
 * for how roadmap entries are labelled in the DB.
 */
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

    /** Returns all volumes defined in the roadmap configuration. */
    public List<VolumeConfig> getAllVolumes() {
        return roadmapProperties.getVolumes();
    }

    /**
     * Looks up a single volume by its URL slug.
     *
     * @throws ResourceNotFoundException if no volume with the given slug exists in the config
     */
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

    /**
     * Maps a single roadmap entry config to its view DTO, annotating it with DB state.
     *
     * <p>Issues one DB query per call via {@code findByNameAndSource}. See the class-level
     * Javadoc for the rationale behind this N+1 approach.
     */
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
