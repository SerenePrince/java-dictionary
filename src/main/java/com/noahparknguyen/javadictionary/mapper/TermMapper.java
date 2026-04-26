package com.noahparknguyen.javadictionary.mapper;

import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.request.UpdateTermRequest;
import com.noahparknguyen.javadictionary.dto.response.TermResponse;
import com.noahparknguyen.javadictionary.model.Term;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class TermMapper {

    public TermResponse toResponse(Term term) {
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
        applyCommonFields(term, request);
        return term;
    }

    public void updateEntityFromRequest(Term term, UpdateTermRequest request) {
        applyCommonFields(term, request);
    }

    private void applyCommonFields(Term term, CreateTermRequest request) {
        term.setName(request.getName());
        term.setCasualDefinition(request.getCasualDefinition());
        term.setFormalDefinition(request.getFormalDefinition());
        term.setExperienceLevel(request.getExperienceLevel());
        term.setTags(request.getTags() != null ? request.getTags() : new HashSet<>());
    }
}