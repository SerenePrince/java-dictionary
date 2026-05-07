package com.noahparknguyen.javadictionary.repository;

import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import com.noahparknguyen.javadictionary.model.TermDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermDefinitionRepository extends JpaRepository<TermDefinition, Long> {

    Optional<TermDefinition> findByTermIdAndExperienceLevel(Long termId, ExperienceLevel level);

    boolean existsByTermIdAndExperienceLevel(Long termId, ExperienceLevel level);
}
