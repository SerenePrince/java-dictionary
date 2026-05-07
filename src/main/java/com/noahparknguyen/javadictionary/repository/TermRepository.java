package com.noahparknguyen.javadictionary.repository;

import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import com.noahparknguyen.javadictionary.model.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {

    List<Term> findByExperienceLevel(ExperienceLevel experienceLevel);

    Optional<Term> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Term> findByTagsContaining(String tag);

    @Query("SELECT t FROM Term t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.casualDefinition) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.formalDefinition) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Term> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT t FROM Term t WHERE t.experienceLevel = :level AND (" +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.casualDefinition) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.formalDefinition) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Term> searchByKeywordAndLevel(@Param("keyword") String keyword, @Param("level") ExperienceLevel level);
}