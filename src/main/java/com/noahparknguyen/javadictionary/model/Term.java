package com.noahparknguyen.javadictionary.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

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
     * URL-safe identifier derived from name (e.g. "Garbage Collection" → "garbage-collection").
     * Multiple Term rows can share the same slug when the same concept appears across different sources.
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

    /**
     * The book this term was written for. Null for manually created terms.
     */
    @Column
    private String sourceBook;

    /**
     * The chapter within the source book. Null for manually created terms.
     */
    @Column
    private String sourceChapter;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "term_tags", joinColumns = @JoinColumn(name = "term_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    /**
     * Returns true if this term was created manually (not sourced from a roadmap book entry).
     */
    public boolean isManual() {
        return sourceBook == null && sourceChapter == null;
    }
}
