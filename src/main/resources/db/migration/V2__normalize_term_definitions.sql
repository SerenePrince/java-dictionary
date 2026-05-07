-- V2: Normalize term definitions
-- Each term is now a concept (name only). Definitions live in term_definitions,
-- keyed by experience level. Tags belong to a definition, not the term itself.

-- New: term_definitions table
CREATE TABLE term_definitions
(
    id                BIGSERIAL PRIMARY KEY,
    term_id           BIGINT        NOT NULL REFERENCES terms (id) ON DELETE CASCADE,
    experience_level  VARCHAR(20)   NOT NULL,
    casual_definition VARCHAR(500)  NOT NULL,
    formal_definition VARCHAR(1000) NOT NULL,
    CONSTRAINT uq_term_level UNIQUE (term_id, experience_level)
);

-- New: per-definition tags
CREATE TABLE term_definition_tags
(
    definition_id BIGINT       NOT NULL REFERENCES term_definitions (id) ON DELETE CASCADE,
    tag           VARCHAR(255) NOT NULL
);

-- Migrate existing term rows into the new structure
INSERT INTO term_definitions (term_id, experience_level, casual_definition, formal_definition)
SELECT id, experience_level, casual_definition, formal_definition
FROM terms;

-- Migrate existing tags (term_tags.term_id -> term_definitions.id via the migrated rows)
INSERT INTO term_definition_tags (definition_id, tag)
SELECT td.id, tt.tag
FROM term_tags tt
         JOIN term_definitions td ON td.term_id = tt.term_id;

-- Drop old columns from terms (now just id + name)
ALTER TABLE terms
    DROP COLUMN casual_definition,
    DROP COLUMN formal_definition,
    DROP COLUMN experience_level;

-- Drop old tags table
DROP TABLE term_tags;
