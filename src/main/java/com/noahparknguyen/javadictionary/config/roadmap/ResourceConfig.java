package com.noahparknguyen.javadictionary.config.roadmap;

/**
 * An external learning resource linked from a roadmap entry.
 */
public class ResourceConfig {

    /** Link label shown to the user. */
    private String title;

    /** Full URL. */
    private String url;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
