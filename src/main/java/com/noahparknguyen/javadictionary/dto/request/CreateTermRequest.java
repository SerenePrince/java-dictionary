package com.noahparknguyen.javadictionary.dto.request;

import com.noahparknguyen.javadictionary.model.ExperienceLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class CreateTermRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be under 100 characters")
    private String name;

    @NotBlank(message = "Casual definition is required")
    @Size(max = 500, message = "Casual definition must be under 500 characters")
    private String casualDefinition;

    @NotBlank(message = "Formal definition is required")
    @Size(max = 1000, message = "Formal definition must be under 1000 characters")
    private String formalDefinition;

    @NotNull(message = "Experience level is required")
    private ExperienceLevel experienceLevel;

    @Size(max = 10, message = "You can have at most 10 tags")
    private Set<@NotBlank String> tags = new HashSet<>();
}
