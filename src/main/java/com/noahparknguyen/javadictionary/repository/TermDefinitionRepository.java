package com.noahparknguyen.javadictionary.repository;

import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import com.noahparknguyen.javadictionary.model.TermDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermDefinitionRepository extends JpaRepository<TermDefinition, Long> {

    boolean existsByTermIdAndExperienceLevel(Long termId, ExperienceLevel level);
}
