package com.noahparknguyen.javadictionary.repository;

import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import com.noahparknguyen.javadictionary.model.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {

    List<Term> findByExperienceLevel(ExperienceLevel experienceLevel);

    Optional<Term> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Term> findByTagsContaining(String tag);

    List<Term> findByNameContainingIgnoreCase(String keyword);

    List<Term> findByExperienceLevelAndNameContainingIgnoreCase(ExperienceLevel level, String keyword);
}