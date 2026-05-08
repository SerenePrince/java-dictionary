package com.noahparknguyen.javadictionary.config.roadmap;

/**
 * Guided research prompts for a roadmap entry.
 * Each field is a question that directs the user's study before they write their definition.
 */
public class HintConfig {

    /** "What is this?" — the concept itself. */
    private String what;

    /** "Why does it matter?" — relevance and importance. */
    private String why;

    /** "How does it work?" — mechanics and usage. */
    private String how;

    public String getWhat() { return what; }
    public void setWhat(String what) { this.what = what; }

    public String getWhy() { return why; }
    public void setWhy(String why) { this.why = why; }

    public String getHow() { return how; }
    public void setHow(String how) { this.how = how; }
}
