package com.noahparknguyen.javadictionary.repository;

import com.noahparknguyen.javadictionary.model.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Data access interface for {@link Term} entities.
 *
 * <p>Custom queries are written in JPQL rather than native SQL so they remain
 * portable across dialects (useful for test contexts running H2). All name
 * comparisons use {@code LOWER()} on both sides to enforce case-insensitive
 * matching while preserving the original casing in storage.
 *
 * <p>Manual-term queries explicitly filter on {@code sourceBook IS NULL AND
 * sourceChapter IS NULL}. This mirrors the partial unique index in the database
 * and is the canonical way to identify manual terms in queries — do not rely on
 * any other heuristic.
 */
public interface TermRepository extends JpaRepository<Term, Long> {

    // ── Slug-based lookups ────────────────────────────────────────────────────

    /**
     * Returns all entries sharing a slug, ordered for consistent display on the
     * group detail page (alphabetical by book, then chapter within each book).
     */
    List<Term> findAllBySlugOrderBySourceBookAscSourceChapterAsc(String slug);

    // ── Uniqueness checks ─────────────────────────────────────────────────────

    /**
     * Returns {@code true} if a manual term with the given name already exists.
     *
     * <p>Manual terms are unique by name alone. A standard {@code UNIQUE} constraint
     * on {@code (name, source_book, source_chapter)} would not enforce this because
     * SQL treats {@code NULL != NULL} — two rows with {@code NULL} source columns
     * would both satisfy the constraint. This query and a matching partial unique
     * index in the DB together close that gap.
     */
    @Query("SELECT COUNT(t) > 0 FROM Term t WHERE LOWER(t.name) = LOWER(:name) AND t.sourceBook IS NULL AND t.sourceChapter IS NULL")
    boolean existsManualByNameIgnoreCase(@Param("name") String name);

    /** Returns {@code true} if a book-sourced term with the given (name, book, chapter) key already exists. */
    @Query("SELECT COUNT(t) > 0 FROM Term t WHERE LOWER(t.name) = LOWER(:name) AND t.sourceBook = :sourceBook AND t.sourceChapter = :sourceChapter")
    boolean existsByNameAndSource(@Param("name") String name,
                                  @Param("sourceBook") String sourceBook,
                                  @Param("sourceChapter") String sourceChapter);

    /** Finds a book-sourced term by its exact (name, book, chapter) composite key. */
    @Query("SELECT t FROM Term t WHERE LOWER(t.name) = LOWER(:name) AND t.sourceBook = :sourceBook AND t.sourceChapter = :sourceChapter")
    Optional<Term> findByNameAndSource(@Param("name") String name,
                                       @Param("sourceBook") String sourceBook,
                                       @Param("sourceChapter") String sourceChapter);

    // ── Filtered list queries (home page) ─────────────────────────────────────

    /** Case-insensitive substring search on term name across all entries. */
    @Query("SELECT t FROM Term t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Term> searchByName(@Param("keyword") String keyword);

    /** Returns all terms from a specific source book. */
    List<Term> findAllBySourceBook(String sourceBook);

    /** Case-insensitive name search scoped to a specific source book. */
    @Query("SELECT t FROM Term t WHERE t.sourceBook = :sourceBook AND LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Term> searchByNameAndBook(@Param("keyword") String keyword,
                                   @Param("sourceBook") String sourceBook);

    /** Returns all manually created terms (both source fields null). */
    @Query("SELECT t FROM Term t WHERE t.sourceBook IS NULL AND t.sourceChapter IS NULL")
    List<Term> findAllManual();

    /** Case-insensitive name search scoped to manually created terms. */
    @Query("SELECT t FROM Term t WHERE t.sourceBook IS NULL AND t.sourceChapter IS NULL AND LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Term> searchManualByName(@Param("keyword") String keyword);
}
