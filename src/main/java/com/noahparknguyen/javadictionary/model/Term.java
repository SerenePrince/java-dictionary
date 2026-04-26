package com.noahparknguyen.javadictionary.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "terms")
@Getter
@Setter
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String casualDefinition;

    @Column(nullable = false, length = 1000)
    private String formalDefinition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceLevel experienceLevel;

    @ElementCollection
    @CollectionTable(name = "term_tags", joinColumns = @JoinColumn(name = "term_id"))
    @Column(name = "tag")
    private Set<String> tags;

    public Term() {
    }
}