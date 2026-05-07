package com.noahparknguyen.javadictionary.controller;

import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.request.UpdateTermRequest;
import com.noahparknguyen.javadictionary.dto.response.TermDefinitionResponse;
import com.noahparknguyen.javadictionary.dto.response.TermResponse;
import com.noahparknguyen.javadictionary.exception.DuplicateResourceException;
import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import com.noahparknguyen.javadictionary.service.TermService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@Slf4j
@Controller
@RequestMapping("/terms")
public class TermViewController {

    private final TermService termService;

    public TermViewController(TermService termService) {
        this.termService = termService;
    }

    // -------------------------------------------------------------------------
    // INDEX
    // -------------------------------------------------------------------------

    @GetMapping
    public String index(
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag,
            Model model) {

        log.debug("GET /terms — level: {}, search: '{}', tag: '{}'",
                experienceLevel != null ? experienceLevel : "none",
                search != null ? search : "none",
                tag != null ? tag : "none");

        model.addAttribute("terms", termService.getFilteredTerms(experienceLevel, search, tag));
        model.addAttribute("experienceLevels", ExperienceLevel.values());
        model.addAttribute("selectedLevel", experienceLevel);
        model.addAttribute("search", search);
        model.addAttribute("selectedTag", tag);
        model.addAttribute("pageTitle", "All Terms");

        return "terms/index";
    }

    // -------------------------------------------------------------------------
    // DETAIL
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.debug("GET /terms/{}", id);
        TermResponse term = termService.getTermById(id);
        model.addAttribute("term", term);
        model.addAttribute("pageTitle", term.name());
        return "terms/detail";
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @GetMapping("/create")
    public String createForm(Model model) {
        log.debug("GET /terms/create");
        model.addAttribute("createTermRequest", new CreateTermRequest());
        model.addAttribute("experienceLevels", ExperienceLevel.values());
        model.addAttribute("pageTitle", "Add Term");
        return "terms/create";
    }

    @PostMapping("/create")
    public String createTerm(
            @Valid @ModelAttribute CreateTermRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            log.warn("POST /terms/create — validation failed");
            model.addAttribute("experienceLevels", ExperienceLevel.values());
            model.addAttribute("pageTitle", "Add Term");
            return "terms/create";
        }

        try {
            log.info("POST /terms/create — name: '{}'", request.getName());
            termService.createTerm(request);
        } catch (DuplicateResourceException e) {
            log.warn("POST /terms/create — duplicate: '{}'", request.getName());
            result.rejectValue("name", "duplicate", e.getMessage());
            model.addAttribute("experienceLevels", ExperienceLevel.values());
            model.addAttribute("pageTitle", "Add Term");
            return "terms/create";
        }

        return "redirect:/terms";
    }

    // -------------------------------------------------------------------------
    // EDIT — level-aware upsert
    // -------------------------------------------------------------------------

    /**
     * Renders the edit page for a term.
     * An optional {@code level} query param controls which level's definitions
     * are pre-filled in the form. Defaults to the first written level.
     */
    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            @RequestParam(required = false) ExperienceLevel level,
            Model model) {

        log.debug("GET /terms/{}/edit — level: {}", id, level);
        TermResponse term = termService.getTermById(id);

        // Resolve which level to display: explicit param > first written level
        ExperienceLevel selectedLevel = level != null
                ? level
                : term.definitions().keySet().stream().findFirst().orElse(ExperienceLevel.ENTRY);

        UpdateTermRequest request = buildUpdateRequest(term, selectedLevel);

        model.addAttribute("term", term);
        model.addAttribute("updateTermRequest", request);
        model.addAttribute("experienceLevels", ExperienceLevel.values());
        model.addAttribute("selectedLevel", selectedLevel);
        model.addAttribute("pageTitle", "Edit Term");

        return "terms/edit";
    }

    @PostMapping("/{id}/edit")
    public String saveDefinition(
            @PathVariable Long id,
            @Valid @ModelAttribute UpdateTermRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            log.warn("POST /terms/{}/edit — validation failed", id);
            TermResponse term = termService.getTermById(id);
            model.addAttribute("term", term);
            model.addAttribute("experienceLevels", ExperienceLevel.values());
            model.addAttribute("selectedLevel", request.getExperienceLevel());
            model.addAttribute("pageTitle", "Edit Term");
            return "terms/edit";
        }

        try {
            log.info("POST /terms/{}/edit — level: {}", id, request.getExperienceLevel());
            termService.saveDefinition(id, request);
        } catch (DuplicateResourceException e) {
            log.warn("POST /terms/{}/edit — duplicate name: '{}'", id, request.getName());
            result.rejectValue("name", "duplicate", e.getMessage());
            TermResponse term = termService.getTermById(id);
            model.addAttribute("term", term);
            model.addAttribute("experienceLevels", ExperienceLevel.values());
            model.addAttribute("selectedLevel", request.getExperienceLevel());
            model.addAttribute("pageTitle", "Edit Term");
            return "terms/edit";
        }

        return "redirect:/terms/" + id;
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/delete")
    public String deleteTerm(@PathVariable Long id) {
        log.info("POST /terms/{}/delete", id);
        termService.deleteTerm(id);
        return "redirect:/terms";
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Builds an UpdateTermRequest pre-filled with the existing definition at
     * {@code level}, or with blank definition fields if none exists yet.
     */
    private UpdateTermRequest buildUpdateRequest(TermResponse term, ExperienceLevel level) {
        UpdateTermRequest request = new UpdateTermRequest();
        request.setName(term.name());
        request.setExperienceLevel(level);

        TermDefinitionResponse existing = term.definitions().get(level);
        if (existing != null) {
            request.setCasualDefinition(existing.casualDefinition());
            request.setFormalDefinition(existing.formalDefinition());
            request.setTags(existing.tags() != null ? new HashSet<>(existing.tags()) : new HashSet<>());
        }

        return request;
    }
}
