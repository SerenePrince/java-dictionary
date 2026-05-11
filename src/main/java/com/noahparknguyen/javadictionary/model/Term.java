package com.noahparknguyen.javadictionary.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Persistent entity representing a single dictionary entry.
 *
 * <p>The data model is intentionally flat — there are no child entities for
 * definitions or experience levels. Instead, the same term name can appear in
 * multiple rows when it is sourced from different books or chapters. The service
 * layer groups these rows by {@code slug} before returning them to callers.
 *
 * <p><b>Manual vs. book-sourced terms:</b> when both {@code sourceBook} and
 * {@code sourceChapter} are {@code null}, the term was added manually by the user
 * (not through the roadmap). The {@link #isManual()} method is the single point
 * of truth for this distinction and drives branching logic in the service and
 * controller layers.
 *
 * <p><b>Uniqueness:</b> the database enforces two separate constraints:
 * <ul>
 *   <li>A composite unique constraint on {@code (name, source_book, source_chapter)}
 *       for book-sourced terms.</li>
 *   <li>A partial unique index on {@code name} where both source columns are
 *       {@code NULL} for manual terms — because SQL {@code NULL != NULL},
 *       a standard unique constraint would not prevent duplicate manual term names.</li>
 * </ul>
 */
@Entity
@Table(name = "terms")
@Getter
@Setter
@NoArgsConstructor
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    @Size(max = 100)
    private String name;

    /**
     * URL-safe identifier derived from {@code name} (e.g. "Garbage Collection" → "garbage-collection").
     * Multiple rows can share the same slug when the same concept appears across different sources.
     * Generated via {@link com.noahparknguyen.javadictionary.mapper.TermMapper#toSlug(String)}.
     */
    @Column(nullable = false)
    private String slug;

    @Column(nullable = false, length = 500)
    @NotBlank
    @Size(max = 500)
    private String casualDefinition;

    @Column(nullable = false, length = 1000)
    @NotBlank
    @Size(max = 1000)
    private String formalDefinition;

    /** Book this term was written for. {@code null} for manually created terms. */
    @Column
    private String sourceBook;

    /** Chapter within {@code sourceBook}. {@code null} for manually created terms. */
    @Column
    private String sourceChapter;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "term_tags", joinColumns = @JoinColumn(name = "term_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    /**
     * Returns {@code true} if this term was created manually rather than through
     * the roadmap. A manual term has no source book or chapter.
     *
     * <p>This flag drives two key behaviors:
     * <ul>
     *   <li>In the edit form: manual terms allow name and tag changes; book-sourced
     *       terms only allow definition edits.</li>
     *   <li>In the uniqueness check: manual terms are deduplicated by name alone,
     *       while book-sourced terms are deduplicated by {@code (name, sourceBook, sourceChapter)}.</li>
     * </ul>
     */
    public boolean isManual() {
        return sourceBook == null && sourceChapter == null;
    }
}
