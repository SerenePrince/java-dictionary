package com.noahparknguyen.javadictionary.controller;

import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.request.UpdateTermRequest;
import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import com.noahparknguyen.javadictionary.service.TermService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
            Model model) {

        log.info("GET /terms - level: {}, search: '{}'",
                experienceLevel != null ? experienceLevel : "none",
                search != null ? search : "none");

        model.addAttribute("terms", termService.getFilteredTerms(experienceLevel, search));
        model.addAttribute("experienceLevels", ExperienceLevel.values());
        model.addAttribute("selectedLevel", experienceLevel);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "All Terms");

        return "terms/index";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.info("GET /terms/{} - fetching detail view", id);
        model.addAttribute("term", termService.getTermById(id));
        model.addAttribute("pageTitle", termService.getTermById(id).getName());
        return "terms/detail";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        log.info("GET /terms/create - loading create form");
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
            log.warn("POST /terms/create - validation failed: {}", result.getFieldErrors());
            model.addAttribute("experienceLevels", ExperienceLevel.values());
            model.addAttribute("pageTitle", "Add Term");
            return "terms/create";
        }

        log.info("POST /terms/create - creating term: '{}'", request.getName());
        termService.createTerm(request);
        return "redirect:/terms";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        log.info("GET /terms/{}/edit - loading edit form", id);
        model.addAttribute("term", termService.getTermById(id));
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
            log.warn("POST /terms/{}/edit - validation failed: {}", id, result.getFieldErrors());
            model.addAttribute("experienceLevels", ExperienceLevel.values());
            model.addAttribute("pageTitle", "Edit Term");
            return "terms/edit";
        }

        log.info("POST /terms/{}/edit - updating term", id);
        termService.updateTerm(id, request);
        return "redirect:/terms/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteTerm(@PathVariable Long id) {
        log.info("POST /terms/{}/delete - deleting term", id);
        termService.deleteTerm(id);
        return "redirect:/terms";
    }
}