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
@Table(
        name = "term_definitions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"term_id", "experience_level"})
)
@Getter
@Setter
@NoArgsConstructor
public class TermDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", nullable = false)
    private ExperienceLevel experienceLevel;

    @Column(nullable = false, length = 500)
    @NotBlank
    @Size(max = 500)
    private String casualDefinition;

    @Column(nullable = false, length = 1000)
    @NotBlank
    @Size(max = 1000)
    private String formalDefinition;

    @ElementCollection
    @CollectionTable(name = "term_definition_tags", joinColumns = @JoinColumn(name = "definition_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();
}
