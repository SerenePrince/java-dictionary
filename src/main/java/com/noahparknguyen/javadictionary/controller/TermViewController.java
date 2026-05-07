package com.noahparknguyen.javadictionary.controller;

import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.request.UpdateTermRequest;
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

    @GetMapping
    public String index(
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag,
            Model model) {

        log.debug("GET /terms - level: {}, search: '{}', tag: '{}'",
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

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.debug("GET /terms/{} - fetching detail view", id);
        TermResponse term = termService.getTermById(id);
        model.addAttribute("term", term);
        model.addAttribute("pageTitle", term.name());
        return "terms/detail";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        log.debug("GET /terms/create - loading create form");
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
            log.warn("POST /terms/create - validation failed - fields: {}",
                    result.getFieldErrors().stream()
                            .map(e -> e.getField() + ": " + e.getDefaultMessage())
                            .toList());
            model.addAttribute("experienceLevels", ExperienceLevel.values());
            model.addAttribute("pageTitle", "Add Term");
            return "terms/create";
        }

        try {
            log.info("POST /terms/create - creating term: '{}'", request.getName());
            termService.createTerm(request);
        } catch (DuplicateResourceException e) {
            log.warn("POST /terms/create - duplicate name: '{}'", request.getName());
            result.rejectValue("name", "duplicate", e.getMessage());
            model.addAttribute("experienceLevels", ExperienceLevel.values());
            model.addAttribute("pageTitle", "Add Term");
            return "terms/create";
        }
        return "redirect:/terms";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        log.debug("GET /terms/{}/edit - loading edit form", id);
        TermResponse term = termService.getTermById(id);

        UpdateTermRequest request = new UpdateTermRequest();
        request.setName(term.name());
        request.setCasualDefinition(term.casualDefinition());
        request.setFormalDefinition(term.formalDefinition());
        request.setExperienceLevel(term.experienceLevel());
        request.setTags(term.tags() != null ? new HashSet<>(term.tags()) : new HashSet<>());

        model.addAttribute("term", term);
        model.addAttribute("updateTermRequest", request);
        model.addAttribute("experienceLevels", ExperienceLevel.values());
        model.addAttribute("pageTitle", "Edit Term");
        return "terms/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateTerm(
            @PathVariable Long id,
            @Valid @ModelAttribute UpdateTermRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            log.warn("POST /terms/{}/edit - validation failed - fields: {}",
                    id,
                    result.getFieldErrors().stream()
                            .map(e -> e.getField() + ": " + e.getDefaultMessage())
                            .toList());
            model.addAttribute("term", termService.getTermById(id));
            model.addAttribute("experienceLevels", ExperienceLevel.values());
            model.addAttribute("pageTitle", "Edit Term");
            return "terms/edit";
        }

        try {
            log.info("POST /terms/{}/edit - updating term", id);
            termService.updateTerm(id, request);
        } catch (DuplicateResourceException e) {
            log.warn("POST /terms/{}/edit - duplicate name: '{}'", id, request.getName());
            result.rejectValue("name", "duplicate", e.getMessage());
            model.addAttribute("term", termService.getTermById(id));
            model.addAttribute("experienceLevels", ExperienceLevel.values());
            model.addAttribute("pageTitle", "Edit Term");
            return "terms/edit";
        }
        return "redirect:/terms/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteTerm(@PathVariable Long id) {
        log.info("POST /terms/{}/delete - deleting term", id);
        termService.deleteTerm(id);
        return "redirect:/terms";
    }
}