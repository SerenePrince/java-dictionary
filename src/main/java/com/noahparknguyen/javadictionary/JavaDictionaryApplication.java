package com.noahparknguyen.javadictionary;

import com.noahparknguyen.javadictionary.config.roadmap.RoadmapProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the Java Dictionary application.
 *
 * <p>Java Dictionary is a personal interview-prep tool for building and reviewing
 * Java terminology. Terms are stored flat in PostgreSQL and grouped by name (slug)
 * at the service layer. A YAML-driven roadmap ties each term to a source book and
 * chapter, giving study sessions structure without hard-coding content in the DB.
 *
 * <p>{@code @EnableConfigurationProperties} is declared here rather than on the
 * config class itself so that {@link RoadmapProperties} is registered as a bean
 * without requiring a full {@code @Component} scan of the config package.
 */
@SpringBootApplication
@EnableConfigurationProperties(RoadmapProperties.class)
public class JavaDictionaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaDictionaryApplication.class, args);
    }
}
