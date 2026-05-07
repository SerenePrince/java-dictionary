package com.noahparknguyen.javadictionary.mapper;

import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.request.UpdateTermRequest;
import com.noahparknguyen.javadictionary.dto.response.TermDefinitionResponse;
import com.noahparknguyen.javadictionary.dto.response.TermResponse;
import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import com.noahparknguyen.javadictionary.model.Term;
import com.noahparknguyen.javadictionary.model.TermDefinition;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TermMapper {

    public TermResponse toResponse(Term term) {
        Map<ExperienceLevel, TermDefinitionResponse> definitions = term.getDefinitions().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> toDefinitionResponse(e.getValue())
                ));
        return new TermResponse(term.getId(), term.getName(), definitions);
    }

    public TermDefinitionResponse toDefinitionResponse(TermDefinition def) {
        return new TermDefinitionResponse(
                def.getId(),
                def.getExperienceLevel(),
                def.getCasualDefinition(),
                def.getFormalDefinition(),
                def.getTags()
        );
    }

    /**
     * Creates a new Term with its first TermDefinition from a create request.
     */
    public Term toEntity(CreateTermRequest request) {
        Term term = new Term();
        term.setName(request.getName());

        TermDefinition definition = buildDefinition(term, request.getExperienceLevel(),
                request.getCasualDefinition(), request.getFormalDefinition(), request.getTags());
        term.getDefinitions().put(request.getExperienceLevel(), definition);

        return term;
    }

    /**
     * Upserts a definition on an existing Term at the level specified in the request.
     * If a definition already exists at that level it is updated in place;
     * if not, a new TermDefinition is created and added to the map.
     */
    public void upsertDefinition(Term term, UpdateTermRequest request) {
        term.setName(request.getName());

        TermDefinition definition = term.getDefinitions().computeIfAbsent(
                request.getExperienceLevel(),
                level -> buildDefinition(term, level, null, null, null)
        );

        definition.setCasualDefinition(request.getCasualDefinition());
        definition.setFormalDefinition(request.getFormalDefinition());
        definition.setTags(request.getTags() != null ? request.getTags() : new HashSet<>());
    }

    // -------------------------------------------------------------------------

    private TermDefinition buildDefinition(Term term, ExperienceLevel level,
                                           String casualDef, String formalDef,
                                           java.util.Set<String> tags) {
        TermDefinition def = new TermDefinition();
        def.setTerm(term);
        def.setExperienceLevel(level);
        def.setCasualDefinition(casualDef);
        def.setFormalDefinition(formalDef);
        def.setTags(tags != null ? tags : new HashSet<>());
        return def;
    }
}
