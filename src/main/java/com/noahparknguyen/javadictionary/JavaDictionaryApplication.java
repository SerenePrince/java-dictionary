package com.noahparknguyen.javadictionary;

import com.noahparknguyen.javadictionary.config.roadmap.RoadmapProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RoadmapProperties.class)
public class JavaDictionaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaDictionaryApplication.class, args);
    }

}
