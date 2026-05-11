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
     * Creates or updates a book-sourced term.
     * If a term already exists for (name, sourceBook, sourceChapter) and override is false,
     * throws IllegalStateException so the caller can ask for confirmation.
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

    @Transactional(readOnly = true)
    public TermResponse getTermById(Long id) {
        return termRepository.findById(id)
                .map(termMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Term", id));
    }

    @Transactional(readOnly = true)
    public TermGroupView getTermGroup(String slug) {
        List<Term> terms = termRepository.findAllBySlugOrderBySourceBookAscSourceChapterAsc(slug);
        if (terms.isEmpty()) {
            throw new ResourceNotFoundException("No terms found for slug: " + slug);
        }
        return termMapper.toGroupView(slug, terms);
    }

    /**
     * Returns all terms grouped by slug, with optional filters.
     *
     * @param search filter by name (case-insensitive substring match)
     * @param tag    filter groups where at least one entry carries this tag
     * @param book   "manual" for manually added terms, book title for book terms, null for all
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
     * Updates a manual term's name, definitions, and tags.
     * For book-sourced terms, only definitions are updated — source fields and tags are locked.
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

    @Transactional
    public void deleteTerm(Long id) {
        log.info("Deleting term id: {}", id);
        if (!termRepository.existsById(id)) {
            throw new ResourceNotFoundException("Term", id);
        }
        termRepository.deleteById(id);
    }
}
