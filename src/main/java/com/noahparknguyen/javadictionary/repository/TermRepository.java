package com.noahparknguyen.javadictionary.repository;

import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import com.noahparknguyen.javadictionary.model.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Term> findByNameIgnoreCase(String name);

    /**
     * Terms that have a definition written at the given level.
     */
    @Query("SELECT DISTINCT t FROM Term t JOIN t.definitions d WHERE d.experienceLevel = :level")
    List<Term> findByDefinitionLevel(@Param("level") ExperienceLevel level);

    /**
     * Full-text search across name and all definition text, any level.
     */
    @Query("SELECT DISTINCT t FROM Term t LEFT JOIN t.definitions d WHERE " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.casualDefinition) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.formalDefinition) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Term> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Full-text search scoped to a specific level's definition.
     */
    @Query("SELECT DISTINCT t FROM Term t JOIN t.definitions d WHERE d.experienceLevel = :level AND (" +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.casualDefinition) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.formalDefinition) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Term> searchByKeywordAndLevel(@Param("keyword") String keyword, @Param("level") ExperienceLevel level);
}
