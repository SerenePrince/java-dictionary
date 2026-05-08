-- V2: Normalize term definitions
-- Each term is now a concept (name only). Definitions live in term_definitions,
-- keyed by experience level. Tags belong to a definition, not the term itself.
--
-- Written to be fully idempotent: safe to run even if partially or fully applied.

-- New: term_definitions table
CREATE TABLE IF NOT EXISTS term_definitions
(
    id                BIGSERIAL PRIMARY KEY,
    term_id           BIGINT        NOT NULL REFERENCES terms (id) ON DELETE CASCADE,
    experience_level  VARCHAR(20)   NOT NULL,
    casual_definition VARCHAR(500)  NOT NULL,
    formal_definition VARCHAR(1000) NOT NULL,
    CONSTRAINT uq_term_level UNIQUE (term_id, experience_level)
);

-- New: per-definition tags
CREATE TABLE IF NOT EXISTS term_definition_tags
(
    definition_id BIGINT       NOT NULL REFERENCES term_definitions (id) ON DELETE CASCADE,
    tag           VARCHAR(255) NOT NULL
);

-- Migrate existing data only if the old columns are still present on terms.
-- This block is skipped safely on re-runs once the columns have been dropped.
DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_name = 'terms'
                     AND column_name = 'experience_level') THEN

            INSERT INTO term_definitions (term_id, experience_level, casual_definition, formal_definition)
            SELECT id, experience_level, casual_definition, formal_definition
            FROM terms;

            INSERT INTO term_definition_tags (definition_id, tag)
            SELECT td.id, tt.tag
            FROM term_tags tt
                     JOIN term_definitions td ON td.term_id = tt.term_id;

        END IF;
    END
$$;

-- Drop old columns if they still exist
ALTER TABLE terms
    DROP COLUMN IF EXISTS casual_definition,
    DROP COLUMN IF EXISTS formal_definition,
    DROP COLUMN IF EXISTS experience_level;

-- Drop old tags table if it still exists
DROP TABLE IF EXISTS term_tags;
