package com.noahparknguyen.javadictionary.controller;

import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.request.UpdateTermRequest;
import com.noahparknguyen.javadictionary.dto.response.TermResponse;
import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import com.noahparknguyen.javadictionary.service.TermService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/terms")
public class TermController {

    private final TermService termService;

    public TermController(TermService termService) {
        this.termService = termService;
    }

    @GetMapping
    public List<TermResponse> getTerms(
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag) {
        log.info("GET /terms - level: {}, search: '{}', tag: '{}'",
                experienceLevel != null ? experienceLevel : "none",
                search != null ? search : "none",
                tag != null ? tag : "none");
        return termService.getFilteredTerms(experienceLevel, search, tag);
    }

    @GetMapping("/{id}")
    public TermResponse getTermById(@PathVariable Long id) {
        log.info("GET /terms/{}", id);
        return termService.getTermById(id);
    }

    @GetMapping("/tag/{tag}")
    public List<TermResponse> getTermsByTag(@PathVariable String tag) {
        log.info("GET /terms/tag/{}", tag);
        return termService.getTermsByTag(tag);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TermResponse createTerm(@Valid @RequestBody CreateTermRequest request) {
        log.info("POST /terms - name: '{}'", request.getName());
        return termService.createTerm(request);
    }

    @PutMapping("/{id}")
    public TermResponse updateTerm(@PathVariable Long id,
                                   @Valid @RequestBody UpdateTermRequest request) {
        log.info("PUT /terms/{}", id);
        return termService.updateTerm(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTerm(@PathVariable Long id) {
        log.info("DELETE /terms/{}", id);
        termService.deleteTerm(id);
    }
}