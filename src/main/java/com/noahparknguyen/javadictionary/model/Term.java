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

    @Column(nullable = false, unique = true)
    @NotBlank
    private String name;

    @Column(nullable = false, length = 500)
    @NotBlank
    @Size(max = 500)
    private String casualDefinition;

    @Column(nullable = false, length = 1000)
    @NotBlank
    @Size(max = 1000)
    private String formalDefinition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceLevel experienceLevel;

    @ElementCollection
    @CollectionTable(name = "term_tags", joinColumns = @JoinColumn(name = "term_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();
}