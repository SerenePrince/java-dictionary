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

/**
 * Converts between {@link Term} entities and their DTO representations.
 *
 * <p>Grouping logic lives here rather than in the service so the service stays
 * focused on business rules and transaction boundaries. The in-memory grouping
 * approach (streaming and collecting by slug) is intentional — the dataset is
 * personal-scale and avoids a second round-trip or a more complex JPQL query.
 */
@Component
public class TermMapper {

    // ── Entity → Response ─────────────────────────────────────────────────────

    /** Maps a single {@link Term} entity to its flat response DTO. */
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
     * Groups a flat list of {@link Term} entities by slug and builds one
     * {@link TermGroupView} per group, sorted alphabetically by slug.
     *
     * <p>The name shown for each group is taken from the first entity in the group.
     * Because all rows sharing a slug were created from the same term name (just
     * different sources), any row's name is representative of the whole group.
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

    /**
     * Builds a {@link TermGroupView} for a pre-fetched list of terms that all
     * share the given slug. Used by the group-detail endpoint where the DB query
     * already constrains the result to a single slug.
     */
    public TermGroupView toGroupView(String slug, List<Term> terms) {
        // Fall back to slug as display name only if the list is unexpectedly empty
        String name = terms.isEmpty() ? slug : terms.get(0).getName();
        List<TermResponse> entries = terms.stream().map(this::toResponse).toList();
        return new TermGroupView(name, slug, entries);
    }

    // ── Request → Entity ──────────────────────────────────────────────────────

    /**
     * Creates a new {@link Term} entity from a create request.
     * {@code sourceBook} and {@code sourceChapter} are left {@code null},
     * marking this as a manual term.
     */
    public Term toEntity(CreateTermRequest request) {
        Term term = new Term();
        term.setName(request.getName());
        term.setSlug(toSlug(request.getName()));
        term.setCasualDefinition(request.getCasualDefinition());
        term.setFormalDefinition(request.getFormalDefinition());
        term.setTags(request.getTags() != null ? request.getTags() : new HashSet<>());
        return term;
    }

    /**
     * Applies all editable fields from a request onto an existing entity in-place.
     * Intended for manual terms only — book-sourced terms apply a narrower update
     * directly in {@link com.noahparknguyen.javadictionary.service.TermService#updateTerm}.
     */
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
     *
     * <p>Transformation steps:
     * <ol>
     *   <li>Trim leading/trailing whitespace</li>
     *   <li>Lowercase</li>
     *   <li>Strip any character that is not a letter, digit, space, or hyphen</li>
     *   <li>Replace runs of whitespace with a single hyphen</li>
     * </ol>
     *
     * <p>Examples:
     * <pre>
     *   "Garbage Collection" → "garbage-collection"
     *   "JVM"               → "jvm"
     *   "if-else"           → "if-else"
     * </pre>
     */
    public static String toSlug(String name) {
        return name.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }
}
