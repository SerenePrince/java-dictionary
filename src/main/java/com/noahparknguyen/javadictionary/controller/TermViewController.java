package com.noahparknguyen.javadictionary.controller;

import com.noahparknguyen.javadictionary.config.roadmap.RoadmapProperties;
import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.response.TermGroupView;
import com.noahparknguyen.javadictionary.dto.response.TermResponse;
import com.noahparknguyen.javadictionary.exception.DuplicateResourceException;
import com.noahparknguyen.javadictionary.service.TermService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/terms")
public class TermViewController {

    private final TermService termService;
    private final RoadmapProperties roadmapProperties;

    public TermViewController(TermService termService, RoadmapProperties roadmapProperties) {
        this.termService = termService;
        this.roadmapProperties = roadmapProperties;
    }

    // ── INDEX ─────────────────────────────────────────────────────────────────

    @GetMapping
    public String index(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String book,
            Model model) {

        log.debug("GET /terms — search: '{}', tag: '{}', book: '{}'",
                search != null ? search : "none",
                tag != null ? tag : "none",
                book != null ? book : "none");

        List<TermGroupView> groups = termService.getFilteredGroups(search, tag, book);

        // Book filter options come from roadmap config so they appear even before terms are added
        List<String> bookOptions = roadmapProperties.getVolumes().stream()
                .map(v -> v.getBook())
                .filter(b -> b != null && !b.isBlank())
                .distinct()
                .sorted()
                .toList();

        model.addAttribute("groups", groups);
        model.addAttribute("bookOptions", bookOptions);
        model.addAttribute("search", search);
        model.addAttribute("selectedTag", tag);
        model.addAttribute("selectedBook", book);
        model.addAttribute("pageTitle", "All Terms");

        return "terms/index";
    }

    // ── GROUP DETAIL ──────────────────────────────────────────────────────────

    @GetMapping("/{slug}")
    public String groupDetail(@PathVariable String slug, Model model) {
        log.debug("GET /terms/{}", slug);
        TermGroupView group = termService.getTermGroup(slug);
        model.addAttribute("group", group);
        model.addAttribute("pageTitle", group.name());
        return "terms/detail";
    }

    // ── CREATE (manual terms only) ────────────────────────────────────────────

    @GetMapping("/create")
    public String createForm(Model model) {
        log.debug("GET /terms/create");
        model.addAttribute("createTermRequest", new CreateTermRequest());
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
            model.addAttribute("pageTitle", "Add Term");
            return "terms/create";
        }

        try {
            log.info("POST /terms/create — name: '{}'", request.getName());
            termService.createTerm(request);
        } catch (DuplicateResourceException e) {
            log.warn("POST /terms/create — duplicate: '{}'", request.getName());
            result.rejectValue("name", "duplicate", e.getMessage());
            model.addAttribute("pageTitle", "Add Term");
            return "terms/create";
        }

        return "redirect:/terms";
    }

    // ── EDIT ──────────────────────────────────────────────────────────────────

    /**
     * Edit form for a single term entry identified by slug + id.
     * Manual terms show all fields; book-sourced terms show definitions only.
     */
    @GetMapping("/{slug}/{id}/edit")
    public String editForm(
            @PathVariable String slug,
            @PathVariable Long id,
            Model model) {

        log.debug("GET /terms/{}/{}/edit", slug, id);
        TermResponse term = termService.getTermById(id);

        CreateTermRequest request = new CreateTermRequest();
        request.setName(term.name());
        request.setCasualDefinition(term.casualDefinition());
        request.setFormalDefinition(term.formalDefinition());
        request.setTags(term.tags());

        model.addAttribute("term", term);
        model.addAttribute("createTermRequest", request);
        model.addAttribute("slug", slug);
        model.addAttribute("pageTitle", "Edit Term");

        return "terms/edit";
    }

    @PostMapping("/{slug}/{id}/edit")
    public String updateTerm(
            @PathVariable String slug,
            @PathVariable Long id,
            @Valid @ModelAttribute CreateTermRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            log.warn("POST /terms/{}/{}/edit — validation failed", slug, id);
            TermResponse term = termService.getTermById(id);
            model.addAttribute("term", term);
            model.addAttribute("slug", slug);
            model.addAttribute("pageTitle", "Edit Term");
            return "terms/edit";
        }

        try {
            log.info("POST /terms/{}/{}/edit — name: '{}'", slug, id, request.getName());
            termService.updateTerm(id, request);
        } catch (DuplicateResourceException e) {
            log.warn("POST /terms/{}/{}/edit — duplicate name: '{}'", slug, id, request.getName());
            result.rejectValue("name", "duplicate", e.getMessage());
            TermResponse term = termService.getTermById(id);
            model.addAttribute("term", term);
            model.addAttribute("slug", slug);
            model.addAttribute("pageTitle", "Edit Term");
            return "terms/edit";
        }

        return "redirect:/terms/" + slug;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * Deletes a single term entry.
     * If other entries remain under the same slug, stays on the group detail page.
     * If the deleted entry was the last one, returns to the index.
     */
    @PostMapping("/{slug}/{id}/delete")
    public String deleteTerm(@PathVariable String slug, @PathVariable Long id) {
        log.info("POST /terms/{}/{}/delete", slug, id);
        termService.deleteTerm(id);

        // Stay on the group page if other entries remain, otherwise go home
        boolean groupIsGone = termService.getFilteredGroups(null, null, null)
                .stream()
                .noneMatch(g -> g.slug().equals(slug));

        return groupIsGone ? "redirect:/terms" : "redirect:/terms/" + slug;
    }
}
