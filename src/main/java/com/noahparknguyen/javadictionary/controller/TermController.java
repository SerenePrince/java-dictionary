package com.noahparknguyen.javadictionary.controller;

import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.response.TermGroupView;
import com.noahparknguyen.javadictionary.dto.response.TermResponse;
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
    public List<TermGroupView> getTerms(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String book) {
        log.info("GET /api/v1/terms — search: '{}', tag: '{}', book: '{}'",
                search != null ? search : "none",
                tag != null ? tag : "none",
                book != null ? book : "none");
        return termService.getFilteredGroups(search, tag, book);
    }

    @GetMapping("/slug/{slug}")
    public TermGroupView getTermGroup(@PathVariable String slug) {
        log.info("GET /api/v1/terms/slug/{}", slug);
        return termService.getTermGroup(slug);
    }

    @GetMapping("/{id}")
    public TermResponse getTermById(@PathVariable Long id) {
        log.info("GET /api/v1/terms/{}", id);
        return termService.getTermById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TermResponse createTerm(@Valid @RequestBody CreateTermRequest request) {
        log.info("POST /api/v1/terms — name: '{}'", request.getName());
        return termService.createTerm(request);
    }

    @PutMapping("/{id}")
    public TermResponse updateTerm(@PathVariable Long id,
                                   @Valid @RequestBody CreateTermRequest request) {
        log.info("PUT /api/v1/terms/{} — name: '{}'", id, request.getName());
        return termService.updateTerm(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTerm(@PathVariable Long id) {
        log.info("DELETE /api/v1/terms/{}", id);
        termService.deleteTerm(id);
    }
}
