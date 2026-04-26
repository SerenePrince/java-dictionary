package com.noahparknguyen.javadictionary.mapper;

import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.response.TermResponse;
import com.noahparknguyen.javadictionary.model.Term;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TermMapper {
    public  TermResponse toResponse(Term term) {
        return new TermResponse(
                term.getId(),
                term.getName(),
                term.getCasualDefinition(),
                term.getFormalDefinition(),
                term.getExperienceLevel(),
                term.getTags()
        );
    }

    public Term toEntity(CreateTermRequest request) {
        Term term = new Term();
        term.setName(request.getName());
        term.setCasualDefinition(request.getCasualDefinition());
        term.setFormalDefinition(request.getFormalDefinition());
        term.setExperienceLevel(request.getExperienceLevel());
        term.setTags(request.getTags() != null ? request.getTags() : Set.of());
        return term;
    }
}