package com.noahparknguyen.javadictionary.service;

import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.request.UpdateTermRequest;
import com.noahparknguyen.javadictionary.dto.response.TermResponse;
import com.noahparknguyen.javadictionary.exception.DuplicateResourceException;
import com.noahparknguyen.javadictionary.exception.ResourceNotFoundException;
import com.noahparknguyen.javadictionary.mapper.TermMapper;
import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import com.noahparknguyen.javadictionary.model.Term;
import com.noahparknguyen.javadictionary.repository.TermRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TermService {

    private final TermRepository termRepository;
    private final TermMapper termMapper;

    public TermService(TermRepository termRepository, TermMapper termMapper) {
        this.termRepository = termRepository;
        this.termMapper = termMapper;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    /**
     * Creates a brand-new term concept together with its first definition.
     * Fails if a term with this name already exists at any level — the user
     * must go to the existing term's edit page to add a new level definition.
     */
    @Transactional
    public TermResponse createTerm(CreateTermRequest request) {
        log.info("Creating term: '{}'", request.getName());
        if (termRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException(
                    "A term named '" + request.getName() + "' already exists. " +
                            "Open the existing term and use the edit page to add a new level definition.");
        }
        Term saved = termRepository.save(termMapper.toEntity(request));
        log.debug("Term created with id: {}", saved.getId());
        return termMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public TermResponse getTermById(Long id) {
        log.debug("Fetching term by id: {}", id);
        return termRepository.findById(id)
                .map(termMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found with id: " + id));
    }

    /**
     * Returns terms that match the given filters.
     *
     * <ul>
     *   <li>level   – restrict to terms that have a definition at this level</li>
     *   <li>keyword – search across name and definition text</li>
     *   <li>tag     – in-memory filter: if level is set, checks that level's tags;
     *                 otherwise checks tags across all levels</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public List<TermResponse> getFilteredTerms(ExperienceLevel level, String keyword, String tag) {
        log.debug("Fetching filtered terms — level: {}, keyword: '{}', tag: '{}'", level, keyword, tag);

        boolean hasLevel = level != null;
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasTag = tag != null && !tag.isBlank();

        List<Term> terms;

        if (hasLevel && hasKeyword) {
            terms = termRepository.searchByKeywordAndLevel(keyword, level);
        } else if (hasLevel) {
            terms = termRepository.findByDefinitionLevel(level);
        } else if (hasKeyword) {
            terms = termRepository.searchByKeyword(keyword);
        } else {
            terms = termRepository.findAll();
        }

        // Tag filter applied in memory — if level is active we check only that
        // level's tags; otherwise any definition's tags count.
        if (hasTag) {
            String tagLower = tag.toLowerCase();
            ExperienceLevel filterLevel = level;
            terms = terms.stream()
                    .filter(t -> {
                        if (filterLevel != null) {
                            var def = t.getDefinitions().get(filterLevel);
                            return def != null && def.getTags().stream()
                                    .anyMatch(tg -> tg.toLowerCase().contains(tagLower));
                        }
                        return t.getDefinitions().values().stream()
                                .anyMatch(d -> d.getTags().stream()
                                        .anyMatch(tg -> tg.toLowerCase().contains(tagLower)));
                    })
                    .toList();
        }

        return terms.stream().map(termMapper::toResponse).toList();
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    /**
     * Saves a definition for an existing term at the level specified in the request.
     * If a definition already exists at that level it is updated in place;
     * if not, a new definition is created (i.e. this is an upsert per level).
     * Also handles term renaming — fails if the new name is already taken.
     */
    @Transactional
    public TermResponse saveDefinition(Long termId, UpdateTermRequest request) {
        log.info("Saving definition for term id: {} at level: {}", termId, request.getExperienceLevel());
        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found with id: " + termId));

        if (!term.getName().equalsIgnoreCase(request.getName())
                && termRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException(
                    "A term named '" + request.getName() + "' already exists");
        }

        termMapper.upsertDefinition(term, request);
        return termMapper.toResponse(termRepository.save(term));
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Transactional
    public void deleteTerm(Long id) {
        log.info("Deleting term id: {}", id);
        if (!termRepository.existsById(id)) {
            throw new ResourceNotFoundException("Term not found with id: " + id);
        }
        termRepository.deleteById(id);
    }
}
