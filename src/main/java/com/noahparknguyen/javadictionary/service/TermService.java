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

    @Transactional
    public TermResponse createTerm(CreateTermRequest request) {
        log.info("Creating term: {}", request.getName());
        Term saved = termRepository.save(termMapper.toEntity(request));
        log.debug("Term created with id: {}", saved.getId());
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

    @Transactional(readOnly = true)
    public List<TermResponse> getFilteredTerms(ExperienceLevel level, String keyword) {
        log.debug("Fetching filtered terms - level: {}, keyword: '{}'", level, keyword);

        boolean hasLevel = level != null;
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasLevel && hasKeyword) {
            return termRepository.findByExperienceLevelAndNameContainingIgnoreCase(level, keyword)
                    .stream().map(termMapper::toResponse).toList();
        } else if (hasLevel) {
            return termRepository.findByExperienceLevel(level)
                    .stream().map(termMapper::toResponse).toList();
        } else if (hasKeyword) {
            return termRepository.findByNameContainingIgnoreCase(keyword)
                    .stream().map(termMapper::toResponse).toList();
        } else {
            return termRepository.findAll()
                    .stream().map(termMapper::toResponse).toList();
        }
    }

    @Transactional(readOnly = true)
    public TermResponse getTermById(Long id) {
        log.debug("Fetching term by id: {}", id);
        return termRepository.findById(id)
                .map(termMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<TermResponse> getTermsByTag(String tag) {
        log.debug("Fetching terms by tag: {}", tag);
        return termRepository.findByTagsContaining(tag)
                .stream()
                .map(termMapper::toResponse)
                .toList();
    }

    @Transactional
    public TermResponse updateTerm(Long id, UpdateTermRequest request) {
        log.info("Updating term id: {}", id);
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found with id: " + id));
        termMapper.updateEntityFromRequest(term, request);
        return termMapper.toResponse(termRepository.save(term));
    }

    @Transactional
    public void deleteTerm(Long id) {
        log.info("Deleting term id: {}", id);
        if (!termRepository.existsById(id)) {
            throw new ResourceNotFoundException("Term not found with id: " + id);
        }
        termRepository.deleteById(id);
    }
}