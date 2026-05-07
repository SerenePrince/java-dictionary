package com.noahparknguyen.javadictionary.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * Definitions keyed by experience level.
     * A term can have at most one definition per level.
     * The map key is the experienceLevel field of TermDefinition.
     */
    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @MapKey(name = "experienceLevel")
    private Map<ExperienceLevel, TermDefinition> definitions = new HashMap<>();
}
