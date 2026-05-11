package com.noahparknguyen.javadictionary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "All term entries sharing the same name/slug, grouped for display. "
        + "A group with multiple entries means the same term appears in more than one source.")
public record TermGroupView(

        @Schema(description = "Display name shared by all entries in this group", example = "Garbage Collection")
        String name,

        @Schema(description = "URL-safe slug shared by all entries in this group", example = "garbage-collection")
        String slug,

        @Schema(description = "Individual entries — one per (sourceBook, sourceChapter) combination, or one for a manual term")
        List<TermResponse> entries

) {
}
