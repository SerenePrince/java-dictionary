package com.noahparknguyen.javadictionary.controller;

import com.noahparknguyen.javadictionary.config.roadmap.EntryConfig;
import com.noahparknguyen.javadictionary.config.roadmap.VolumeConfig;
import com.noahparknguyen.javadictionary.service.RoadmapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Thymeleaf MVC controller for the roadmap UI.
 *
 * <p>Renders the roadmap index (volume cards) and per-volume chapter pages. It also
 * handles the inline definition submit form on each chapter page, which is the primary
 * way book-sourced terms enter the dictionary.
 *
 * <p><b>Override flow:</b> when a user submits a definition for a term that already has
 * one, {@link RoadmapService#submitEntry} throws an {@link IllegalStateException}. This
 * controller catches that, stores the pending form values as flash attributes, and
 * redirects back to the volume page, where the template re-populates the form and shows
 * a confirmation checkbox. The user submits again with {@code override=true} to proceed.
 * Flash attributes are used (rather than model attributes) so that a browser refresh after
 * the redirect does not re-submit the form.
 */
@Slf4j
@Controller
@RequestMapping("/roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    // ── Index — list all volumes ──────────────────────────────────────────────

    @GetMapping
    public String index(Model model) {
        log.debug("GET /roadmap");
        model.addAttribute("volumes", roadmapService.getAllVolumes());
        model.addAttribute("pageTitle", "Roadmap");
        return "roadmap/index";
    }

    // ── Volume page — chapters + accordion entries ────────────────────────────

    @GetMapping("/{volumeSlug}")
    public String volume(@PathVariable String volumeSlug, Model model) {
        log.debug("GET /roadmap/{}", volumeSlug);
        VolumeConfig volume = roadmapService.getVolume(volumeSlug);
        model.addAttribute("volume", volume);
        model.addAttribute("chapters", roadmapService.getChapterViews(volumeSlug));
        model.addAttribute("pageTitle", volume.getName());
        return "roadmap/volume";
    }

    // ── Submit a definition from the roadmap mini-form ────────────────────────

    @PostMapping("/{volumeSlug}/submit")
    public String submit(
            @PathVariable String volumeSlug,
            @RequestParam String termName,
            @RequestParam String chapterName,
            @RequestParam String casualDefinition,
            @RequestParam String formalDefinition,
            @RequestParam(defaultValue = "false") boolean override,
            RedirectAttributes redirectAttributes) {

        log.info("POST /roadmap/{}/submit — term: '{}', chapter: '{}', override: {}",
                volumeSlug, termName, chapterName, override);

        EntryConfig entry = roadmapService.findEntry(volumeSlug, chapterName, termName);

        try {
            roadmapService.submitEntry(volumeSlug, chapterName, entry, casualDefinition, formalDefinition, override);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Definition for '" + termName + "' saved successfully!");
        } catch (IllegalStateException e) {
            // Term already has a definition at this (book, chapter) — ask for confirmation
            redirectAttributes.addFlashAttribute("overrideWarning", e.getMessage());
            redirectAttributes.addFlashAttribute("pendingTermName", termName);
            redirectAttributes.addFlashAttribute("pendingChapterName", chapterName);
            redirectAttributes.addFlashAttribute("pendingCasualDefinition", casualDefinition);
            redirectAttributes.addFlashAttribute("pendingFormalDefinition", formalDefinition);
        }

        return "redirect:/roadmap/" + volumeSlug;
    }
}
