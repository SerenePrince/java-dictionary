package com.noahparknguyen.javadictionary.controller;

import com.noahparknguyen.javadictionary.dto.request.CreateTermRequest;
import com.noahparknguyen.javadictionary.dto.response.TermGroupView;
import com.noahparknguyen.javadictionary.dto.response.TermResponse;
import com.noahparknguyen.javadictionary.exception.ErrorResponse;
import com.noahparknguyen.javadictionary.service.TermService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for term management.
 *
 * <p>Exposes CRUD operations on dictionary terms under {@code /api/v1/terms}.
 * All endpoints return and consume JSON. This controller is intentionally thin —
 * validation is handled by Bean Validation annotations on the request DTO,
 * exception mapping lives in {@link com.noahparknguyen.javadictionary.exception.GlobalExceptionHandler},
 * and business logic is in {@link com.noahparknguyen.javadictionary.service.TermService}.
 *
 * <p>This controller is excluded from Thymeleaf-based routing and is the only
 * class scanned by SpringDoc for the Swagger UI. Path matching is narrowed to
 * {@code /api/v1/**} via {@code springdoc.paths-to-match} in {@code application.properties}.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/terms")
@Tag(name = "Terms", description = "Create, retrieve, update, and delete Java dictionary terms")
public class TermController {

    private final TermService termService;

    public TermController(TermService termService) {
        this.termService = termService;
    }

    // ── LIST ──────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(
            summary = "List all term groups",
            description = """
                    Returns every term grouped by slug, one group per unique name. \
                    All three filter parameters are optional and combinable. \
                    Pass `book=manual` to list only manually added terms."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups returned (empty list if no matches)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TermGroupView.class))))
    })
    public List<TermGroupView> getTerms(
            @Parameter(description = "Case-insensitive substring match on term name", example = "garbage")
            @RequestParam(required = false) String search,

            @Parameter(description = "Case-insensitive substring match on any tag", example = "jvm")
            @RequestParam(required = false) String tag,

            @Parameter(description = "Exact source book title, or `manual` for manually added terms",
                    example = "Core Java Vol I")
            @RequestParam(required = false) String book) {

        log.info("GET /api/v1/terms — search: '{}', tag: '{}', book: '{}'",
                search != null ? search : "none",
                tag != null ? tag : "none",
                book != null ? book : "none");
        return termService.getFilteredGroups(search, tag, book);
    }

    // ── GROUP DETAIL ──────────────────────────────────────────────────────────

    @GetMapping("/slug/{slug}")
    @Operation(
            summary = "Get a term group by slug",
            description = "Returns all entries sharing the given slug, ordered by source book then chapter."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TermGroupView.class))),
            @ApiResponse(responseCode = "404", description = "No terms found for this slug",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TermGroupView getTermGroup(
            @Parameter(description = "URL-safe slug (e.g. `garbage-collection`)", example = "garbage-collection")
            @PathVariable String slug) {

        log.info("GET /api/v1/terms/slug/{}", slug);
        return termService.getTermGroup(slug);
    }

    // ── SINGLE ENTRY ──────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get a single term entry by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Term found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TermResponse.class))),
            @ApiResponse(responseCode = "404", description = "Term not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TermResponse getTermById(
            @Parameter(description = "Term entry ID", example = "1")
            @PathVariable Long id) {

        log.info("GET /api/v1/terms/{}", id);
        return termService.getTermById(id);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a manual term",
            description = """
                    Creates a new manually added term with no source book or chapter. \
                    Name uniqueness is enforced case-insensitively across all manual terms. \
                    Book-sourced terms are created automatically by the roadmap submit flow."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Term created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TermResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed — see `errors` map in response",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "A manual term with this name already exists",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TermResponse createTerm(@Valid @RequestBody CreateTermRequest request) {
        log.info("POST /api/v1/terms — name: '{}'", request.getName());
        return termService.createTerm(request);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a term entry",
            description = """
                    For **manual terms**: updates name, both definitions, and tags. \
                    Name change is rejected if another manual term already has that name. \
                    For **book-sourced terms**: only the casual and formal definitions are updated. \
                    Name, source fields, and tags are managed by the roadmap config and cannot be changed here."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Term updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TermResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Term not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Name conflicts with an existing manual term",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TermResponse updateTerm(
            @Parameter(description = "Term entry ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CreateTermRequest request) {

        log.info("PUT /api/v1/terms/{} — name: '{}'", id, request.getName());
        return termService.updateTerm(id, request);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a term entry by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Term deleted"),
            @ApiResponse(responseCode = "404", description = "Term not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void deleteTerm(
            @Parameter(description = "Term entry ID", example = "1")
            @PathVariable Long id) {

        log.info("DELETE /api/v1/terms/{}", id);
        termService.deleteTerm(id);
    }
}
