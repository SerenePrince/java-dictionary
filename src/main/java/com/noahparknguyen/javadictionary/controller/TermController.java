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
    public List<TermResponse> getTerms(@RequestParam(required = false) ExperienceLevel experienceLevel) {
        log.info("GET /terms - filter: {}", experienceLevel != null ? experienceLevel : "none");
        return experienceLevel != null
                ? termService.getTermsByExperienceLevel(experienceLevel)
                : termService.getAllTerms();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public TermResponse createTerm(@Valid @RequestBody CreateTermRequest request) {
        log.info("POST /terms - name: '{}'", request.getName());
        return termService.createTerm(request);
    }

    @GetMapping("/{id}")
    public TermResponse getTermById(@PathVariable Long id) {
        log.info("GET /api/v1/terms/{}", id);
        return termService.getTermById(id);
    }

    @PutMapping("/{id}")
    public TermResponse updateTerm(@PathVariable Long id,
                                   @Valid @RequestBody UpdateTermRequest request) {
        log.info("PUT /api/v1/terms/{}", id);
        return termService.updateTerm(id, request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteTerm(@PathVariable Long id) {
        log.info("DELETE /api/v1/terms/{}", id);
        termService.deleteTerm(id);
    }
}
