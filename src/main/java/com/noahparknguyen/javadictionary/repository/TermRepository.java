package com.noahparknguyen.javadictionary.repository;

import com.noahparknguyen.javadictionary.model.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {

    // ── Slug-based lookups (group detail page) ────────────────────────────────

    List<Term> findAllBySlugOrderBySourceBookAscSourceChapterAsc(String slug);

    // ── Uniqueness checks ─────────────────────────────────────────────────────

    /** Manual terms: unique by name alone (both source fields null). */
    @Query("SELECT COUNT(t) > 0 FROM Term t WHERE LOWER(t.name) = LOWER(:name) AND t.sourceBook IS NULL AND t.sourceChapter IS NULL")
    boolean existsManualByNameIgnoreCase(@Param("name") String name);

    /** Book terms: unique by (name, sourceBook, sourceChapter). */
    @Query("SELECT COUNT(t) > 0 FROM Term t WHERE LOWER(t.name) = LOWER(:name) AND t.sourceBook = :sourceBook AND t.sourceChapter = :sourceChapter")
    boolean existsByNameAndSource(@Param("name") String name,
                                  @Param("sourceBook") String sourceBook,
                                  @Param("sourceChapter") String sourceChapter);

    /** Find a book-sourced term by its exact (name, sourceBook, sourceChapter) key. */
    @Query("SELECT t FROM Term t WHERE LOWER(t.name) = LOWER(:name) AND t.sourceBook = :sourceBook AND t.sourceChapter = :sourceChapter")
    Optional<Term> findByNameAndSource(@Param("name") String name,
                                       @Param("sourceBook") String sourceBook,
                                       @Param("sourceChapter") String sourceChapter);

    // ── Filtered list queries (home page) ─────────────────────────────────────

    /** Name search across all terms. */
    @Query("SELECT t FROM Term t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Term> searchByName(@Param("keyword") String keyword);

    /** Book filter — all terms from a specific source book. */
    List<Term> findAllBySourceBook(String sourceBook);

    /** Name search scoped to a source book. */
    @Query("SELECT t FROM Term t WHERE t.sourceBook = :sourceBook AND LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Term> searchByNameAndBook(@Param("keyword") String keyword,
                                   @Param("sourceBook") String sourceBook);

    /** Manual-only terms (both source fields null). */
    @Query("SELECT t FROM Term t WHERE t.sourceBook IS NULL AND t.sourceChapter IS NULL")
    List<Term> findAllManual();

    /** Name search scoped to manual terms. */
    @Query("SELECT t FROM Term t WHERE t.sourceBook IS NULL AND t.sourceChapter IS NULL AND LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Term> searchManualByName(@Param("keyword") String keyword);
}
