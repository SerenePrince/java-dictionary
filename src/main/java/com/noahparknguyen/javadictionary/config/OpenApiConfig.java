package com.noahparknguyen.javadictionary.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI 3 specification exposed at {@code /api-docs}.
 *
 * <p>SpringDoc scans the classpath automatically; this bean provides the
 * top-level metadata (title, version, contact) that appears in Swagger UI.
 * Path filtering ({@code springdoc.paths-to-match=/api/v1/**}) in
 * {@code application.properties} ensures only REST endpoints are included —
 * Thymeleaf MVC controllers are excluded without needing {@code @Hidden}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Java Dictionary API")
                        .description("""
                                REST API for managing Java terminology.

                                Terms are stored in a flat model and grouped by name (slug) for display. \
                                A single term name can have multiple entries — one per source book and chapter \
                                (book-sourced terms) or a single entry with no source (manual terms).

                                The Swagger UI below covers all REST endpoints. \
                                The Thymeleaf UI is served separately at /terms and /roadmap.""")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Noah Park-Nguyen")
                                .email("noahparknguyen@gmail.com")));
    }
}
