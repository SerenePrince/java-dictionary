package com.noahparknguyen.javadictionary.service;

import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.response.TermGroupView;
import com.noahparknguyen.javadictionary.dto.response.TermResponse;
import com.noahparknguyen.javadictionary.exception.DuplicateResourceException;
import com.noahparknguyen.javadictionary.exception.ResourceNotFoundException;
import com.noahparknguyen.javadictionary.mapper.TermMapper;
import com.noahparknguyen.javadictionary.model.Term;
import com.noahparknguyen.javadictionary.repository.TermRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Business logic layer for dictionary term management.
 *
 * <p>This service is the single point of truth for create, read, update, and
 * delete operations on {@link com.noahparknguyen.javadictionary.model.Term} entities.
 * It enforces uniqueness rules, controls which fields can be edited on manual vs.
 * book-sourced terms, and delegates all entity↔DTO conversion to {@link TermMapper}.
 *
 * <p><b>Manual vs. book-sourced terms:</b> the distinction drives two different code
 * paths throughout this class. Manual terms allow name and tag changes; book-sourced
 * terms lock those fields and only allow definition edits. See
 * {@link com.noahparknguyen.javadictionary.model.Term#isManual()} for the canonical
 * definition of "manual."
 *
 * <p><b>Grouping:</b> the flat DB rows are grouped by slug at the mapper layer, not
 * here. This service returns grouped views to callers by passing a raw {@code List<Term>}
 * to {@link TermMapper#toGroupViews} or {@link TermMapper#toGroupView}.
 */
@Slf4j
@Service
public class TermService {

    private final TermRepository termRepository;
    private final TermMapper termMapper;

    public TermService(TermRepository termRepository, TermMapper termMapper) {
        this.termRepository = termRepository;
        this.termMapper = termMapper;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Creates a new manual term (no source book or chapter).
     * Blocks if a manual term with the same name already exists.
     */
    @Transactional
    public TermResponse createTerm(CreateTermRequest request) {
        log.info("Creating manual term: '{}'", request.getName());
        if (termRepository.existsManualByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException(
                    "A manual term named '" + request.getName() + "' already exists.");
        }
        Term saved = termRepository.save(termMapper.toEntity(request));
        log.debug("Manual term created with id: {}", saved.getId());
        return termMapper.toResponse(saved);
    }

    /**
     * Creates or updates a book-sourced term identified by the composite key
     * {@code (name, sourceBook, sourceChapter)}.
     *
     * <p><b>Override semantics:</b> if a term with the same composite key already exists
     * and {@code override} is {@code false}, an {@link IllegalStateException} is thrown
     * with a user-facing message prompting confirmation. The roadmap controller catches
     * this and re-renders the submit form with a confirmation checkbox, allowing the user
     * to resubmit with {@code override = true} to update the existing entry's definitions.
     *
     * <p>Source fields ({@code sourceBook}, {@code sourceChapter}) and tags are set on
     * creation but are not modified during an override — only {@code casualDefinition}
     * and {@code formalDefinition} are updated.
     *
     * @param override {@code true} to update an existing entry; {@code false} to fail fast
     * @throws IllegalStateException if a matching term exists and {@code override} is false
     */
    @Transactional
    public TermResponse saveBookTerm(String name, String casualDefinition, String formalDefinition,
                                     String sourceBook, String sourceChapter,
                                     Set<String> tags, boolean override) {
        log.info("Saving book term: '{}' [{} / {}]", name, sourceBook, sourceChapter);

        return termRepository.findByNameAndSource(name, sourceBook, sourceChapter)
                .map(existing -> {
                    if (!override) {
                        throw new IllegalStateException(
                                "A definition for '" + name + "' already exists in "
                                        + sourceChapter + ". Submit again to override it.");
                    }
                    existing.setCasualDefinition(casualDefinition);
                    existing.setFormalDefinition(formalDefinition);
                    existing.setTags(tags != null ? tags : new HashSet<>());
                    return termMapper.toResponse(termRepository.save(existing));
                })
                .orElseGet(() -> {
                    Term term = new Term();
                    term.setName(name);
                    term.setSlug(TermMapper.toSlug(name));
                    term.setCasualDefinition(casualDefinition);
                    term.setFormalDefinition(formalDefinition);
                    term.setSourceBook(sourceBook);
                    term.setSourceChapter(sourceChapter);
                    term.setTags(tags != null ? tags : new HashSet<>());
                    return termMapper.toResponse(termRepository.save(term));
                });
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /**
     * Returns a single term by its primary key.
     *
     * @throws ResourceNotFoundException if no term with the given id exists
     */
    @Transactional(readOnly = true)
    public TermResponse getTermById(Long id) {
        return termRepository.findById(id)
                .map(termMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Term", id));
    }

    /**
     * Returns all entries sharing the given slug, grouped into a {@link TermGroupView}.
     * Results are ordered by source book then chapter (see repository query).
     *
     * @throws ResourceNotFoundException if no terms match the slug
     */
    @Transactional(readOnly = true)
    public TermGroupView getTermGroup(String slug) {
        List<Term> terms = termRepository.findAllBySlugOrderBySourceBookAscSourceChapterAsc(slug);
        if (terms.isEmpty()) {
            throw new ResourceNotFoundException("No terms found for slug: " + slug);
        }
        return termMapper.toGroupView(slug, terms);
    }

    /**
     * Returns all term groups matching the given filters, grouped by slug and sorted alphabetically.
     *
     * <p>Filters are applied at the DB layer where possible (search + book combinations map
     * to distinct repository queries). The tag filter has no matching index so it is applied
     * in-memory after the DB fetch — acceptable at personal scale.
     *
     * <p><b>Filter combinations:</b>
     * <ul>
     *   <li>Both {@code search} and {@code book}: scoped name search</li>
     *   <li>{@code book} only: all terms from that source</li>
     *   <li>{@code search} only: name search across all sources</li>
     *   <li>Neither: all terms</li>
     * </ul>
     *
     * @param search case-insensitive substring match on term name; {@code null} or blank to skip
     * @param tag    keep only entries that carry this tag (case-insensitive); {@code null} to skip
     * @param book   {@code "manual"} for manually added terms, a book title for book-sourced terms,
     *               or {@code null} for all terms
     */
    @Transactional(readOnly = true)
    public List<TermGroupView> getFilteredGroups(String search, String tag, String book) {
        log.debug("Fetching filtered groups — search: '{}', tag: '{}', book: '{}'", search, tag, book);

        boolean hasSearch = search != null && !search.isBlank();
        boolean hasBook = book != null && !book.isBlank();
        boolean isManualFilter = "manual".equalsIgnoreCase(book);

        List<Term> terms;

        if (hasBook && hasSearch) {
            terms = isManualFilter
                    ? termRepository.searchManualByName(search)
                    : termRepository.searchByNameAndBook(search, book);
        } else if (hasBook) {
            terms = isManualFilter
                    ? termRepository.findAllManual()
                    : termRepository.findAllBySourceBook(book);
        } else if (hasSearch) {
            terms = termRepository.searchByName(search);
        } else {
            terms = termRepository.findAll();
        }

        // Tag filter — keep entries where at least one tag matches
        if (tag != null && !tag.isBlank()) {
            String tagLower = tag.toLowerCase();
            terms = terms.stream()
                    .filter(t -> t.getTags().stream()
                            .anyMatch(tg -> tg.toLowerCase().contains(tagLower)))
                    .toList();
        }

        return termMapper.toGroupViews(terms);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /**
     * Updates an existing term by its id, applying different rules based on term type.
     *
     * <p><b>Manual terms</b> allow all fields to change: name, definitions, and tags.
     * If the name changes, a duplicate-name check is performed against other manual terms.
     *
     * <p><b>Book-sourced terms</b> only allow definition edits. Name, source fields, and
     * tags are locked because they are derived from the roadmap YAML and changing them
     * manually would cause drift between the DB and the config.
     *
     * @throws ResourceNotFoundException  if no term with the given id exists
     * @throws DuplicateResourceException if a manual term's name is changed to one that already exists
     */
    @Transactional
    public TermResponse updateTerm(Long id, CreateTermRequest request) {
        log.info("Updating term id: {}", id);
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term", id));

        if (term.isManual()) {
            // Manual: allow name + tags to change, check no collision
            if (!term.getName().equalsIgnoreCase(request.getName())
                    && termRepository.existsManualByNameIgnoreCase(request.getName())) {
                throw new DuplicateResourceException(
                        "A manual term named '" + request.getName() + "' already exists.");
            }
            termMapper.updateEntity(term, request);
        } else {
            // Book-sourced: only definitions are editable
            term.setCasualDefinition(request.getCasualDefinition());
            term.setFormalDefinition(request.getFormalDefinition());
        }

        return termMapper.toResponse(termRepository.save(term));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * Deletes a term by its primary key.
     *
     * @throws ResourceNotFoundException if no term with the given id exists
     */
    @Transactional
    public void deleteTerm(Long id) {
        log.info("Deleting term id: {}", id);
        if (!termRepository.existsById(id)) {
            throw new ResourceNotFoundException("Term", id);
        }
        termRepository.deleteById(id);
    }
}
