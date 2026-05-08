package com.noahparknguyen.javadictionary.config.roadmap;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Root configuration properties for the Java learning roadmap.
 * Bound from src/main/resources/roadmap.yaml.
 */
@ConfigurationProperties(prefix = "roadmap")
public class RoadmapProperties {

    private List<VolumeConfig> volumes = new ArrayList<>();

    public List<VolumeConfig> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<VolumeConfig> volumes) {
        this.volumes = volumes;
    }
}
