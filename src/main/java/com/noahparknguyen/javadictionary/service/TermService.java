package com.noahparknguyen.javadictionary.service;

import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.request.UpdateTermRequest;
import com.noahparknguyen.javadictionary.dto.response.TermResponse;
import com.noahparknguyen.javadictionary.exception.ResourceNotFoundException;
import com.noahparknguyen.javadictionary.mapper.TermMapper;
import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import com.noahparknguyen.javadictionary.model.Term;
import com.noahparknguyen.javadictionary.repository.TermRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

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

    @Transactional
    public TermResponse createTerm(CreateTermRequest request) {
        log.info("Creating term with name: {}", request.getName());
        Term saved = termRepository.save(termMapper.toEntity(request));
        log.debug("Term saved with id: {}", saved.getId());
        return termMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TermResponse> getAllTerms() {
        log.debug("Fetching all terms");
        return termRepository.findAll()
                .stream()
                .map(termMapper::toResponse)
                .toList();
    }

    public List<TermResponse> getTermsByExperienceLevel(ExperienceLevel level) {
        log.debug("Fetching terms for level: {}", level);
        return termRepository.findByExperienceLevel(level)
                .stream()
                .map(termMapper::toResponse)
                .toList();
    }

    // Filtered terms for the index page
    public List<TermResponse> getFilteredTerms(ExperienceLevel level, String search) {
        log.debug("Fetching filtered terms - level: {}, search: '{}'", level, search);

        return termRepository.findAll()
                .stream()
                .filter(term -> level == null || term.getExperienceLevel() == level)
                .filter(term -> search == null || search.isBlank() ||
                        term.getName().toLowerCase().contains(search.toLowerCase()))
                .map(termMapper::toResponse)
                .toList();
    }

    // Single term for detail and edit pages
    public TermResponse getTermById(Long id) {
        log.debug("Fetching term by id: {}", id);
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Term not found with id: " + id));
        return termMapper.toResponse(term);
    }

    // Update
    @Transactional
    public TermResponse updateTerm(Long id, UpdateTermRequest request) {
        log.info("Updating term with id: {}", id);
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Term not found with id: " + id));

        term.setName(request.getName());
        term.setCasualDefinition(request.getCasualDefinition());
        term.setFormalDefinition(request.getFormalDefinition());
        term.setExperienceLevel(request.getExperienceLevel());
        term.setTags(request.getTags() != null ? request.getTags() : Set.of());

        return termMapper.toResponse(termRepository.save(term));
    }

    // Delete
    @Transactional
    public void deleteTerm(Long id) {
        log.info("Deleting term with id: {}", id);
        if (!termRepository.existsById(id)) {
            throw new ResourceNotFoundException("Term not found with id: " + id);
        }
        termRepository.deleteById(id);
    }
}
