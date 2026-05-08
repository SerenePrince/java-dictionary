package com.noahparknguyen.javadictionary.controller;

import com.noahparknguyen.javadictionary.config.roadmap.EntryConfig;
import com.noahparknguyen.javadictionary.config.roadmap.VolumeConfig;
import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import com.noahparknguyen.javadictionary.service.RoadmapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    // -------------------------------------------------------------------------
    // Index — list all volumes
    // -------------------------------------------------------------------------

    @GetMapping
    public String index(Model model) {
        log.debug("GET /roadmap");
        model.addAttribute("volumes", roadmapService.getAllVolumes());
        model.addAttribute("pageTitle", "Roadmap");
        return "roadmap/index";
    }

    // -------------------------------------------------------------------------
    // Volume page — chapters + accordion entries
    // -------------------------------------------------------------------------

    @GetMapping("/{volumeSlug}")
    public String volume(@PathVariable String volumeSlug, Model model) {
        log.debug("GET /roadmap/{}", volumeSlug);
        VolumeConfig volume = roadmapService.getVolume(volumeSlug);
        model.addAttribute("volume", volume);
        model.addAttribute("chapters", roadmapService.getChapterViews(volumeSlug));
        model.addAttribute("pageTitle", volume.getName());
        return "roadmap/volume";
    }

    // -------------------------------------------------------------------------
    // Submit a definition from the roadmap mini-form
    // -------------------------------------------------------------------------

    @PostMapping("/{volumeSlug}/submit")
    public String submit(
            @PathVariable String volumeSlug,
            @RequestParam String termName,
            @RequestParam ExperienceLevel experienceLevel,
            @RequestParam String casualDefinition,
            @RequestParam String formalDefinition,
            @RequestParam(defaultValue = "false") boolean override,
            RedirectAttributes redirectAttributes) {

        log.info("POST /roadmap/{}/submit — term: '{}', level: {}, override: {}",
                volumeSlug, termName, experienceLevel, override);

        EntryConfig entry = roadmapService.findEntry(volumeSlug, termName, experienceLevel);

        try {
            roadmapService.submitEntry(entry, casualDefinition, formalDefinition, override);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Definition for '" + termName + "' saved successfully!");
        } catch (IllegalStateException e) {
            // Term already has a definition at this level — ask for confirmation
            redirectAttributes.addFlashAttribute("overrideWarning", e.getMessage());
            redirectAttributes.addFlashAttribute("pendingTermName", termName);
            redirectAttributes.addFlashAttribute("pendingLevel", experienceLevel);
            redirectAttributes.addFlashAttribute("pendingCasualDefinition", casualDefinition);
            redirectAttributes.addFlashAttribute("pendingFormalDefinition", formalDefinition);
        }

        return "redirect:/roadmap/" + volumeSlug;
    }
}
