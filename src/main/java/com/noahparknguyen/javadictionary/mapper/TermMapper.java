package com.noahparknguyen.javadictionary.mapper;

import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.response.TermGroupView;
import com.noahparknguyen.javadictionary.dto.response.TermResponse;
import com.noahparknguyen.javadictionary.model.Term;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TermMapper {

    // ── Entity → Response ─────────────────────────────────────────────────────

    public TermResponse toResponse(Term term) {
        return new TermResponse(
                term.getId(),
                term.getName(),
                term.getSlug(),
                term.getCasualDefinition(),
                term.getFormalDefinition(),
                term.getSourceBook(),
                term.getSourceChapter(),
                term.getTags(),
                term.isManual()
        );
    }

    /**
     * Groups a flat list of Term entities by slug and builds a TermGroupView per group.
     * Groups are returned in alphabetical slug order.
     */
    public List<TermGroupView> toGroupViews(List<Term> terms) {
        Map<String, List<Term>> bySlug = terms.stream()
                .collect(Collectors.groupingBy(Term::getSlug));

        return bySlug.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<Term> group = entry.getValue();
                    String name = group.get(0).getName();
                    List<TermResponse> entries = group.stream()
                            .map(this::toResponse)
                            .toList();
                    return new TermGroupView(name, entry.getKey(), entries);
                })
                .toList();
    }

    public TermGroupView toGroupView(String slug, List<Term> terms) {
        String name = terms.isEmpty() ? slug : terms.get(0).getName();
        List<TermResponse> entries = terms.stream().map(this::toResponse).toList();
        return new TermGroupView(name, slug, entries);
    }

    // ── Request → Entity ──────────────────────────────────────────────────────

    public Term toEntity(CreateTermRequest request) {
        Term term = new Term();
        term.setName(request.getName());
        term.setSlug(toSlug(request.getName()));
        term.setCasualDefinition(request.getCasualDefinition());
        term.setFormalDefinition(request.getFormalDefinition());
        term.setTags(request.getTags() != null ? request.getTags() : new HashSet<>());
        // sourceBook and sourceChapter remain null for manual terms
        return term;
    }

    public void updateEntity(Term term, CreateTermRequest request) {
        term.setName(request.getName());
        term.setSlug(toSlug(request.getName()));
        term.setCasualDefinition(request.getCasualDefinition());
        term.setFormalDefinition(request.getFormalDefinition());
        term.setTags(request.getTags() != null ? request.getTags() : new HashSet<>());
    }

    // ── Slug utility ──────────────────────────────────────────────────────────

    /**
     * Converts a term name to a URL-safe slug.
     * Examples:  "Garbage Collection" → "garbage-collection"
     *            "JVM"               → "jvm"
     *            "if-else"           → "if-else"
     */
    public static String toSlug(String name) {
        return name.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }
}
