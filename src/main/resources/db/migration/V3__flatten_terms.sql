-- V3: Flatten terms — collapse term_definitions back into terms,
-- add source_book / source_chapter / slug, retire experience_level.
-- Fully idempotent: safe to run on a database that is already at this state.

-- 1. Add new columns to terms (ignored if already present)
ALTER TABLE terms ADD COLUMN IF NOT EXISTS casual_definition  VARCHAR(500);
ALTER TABLE terms ADD COLUMN IF NOT EXISTS formal_definition  VARCHAR(1000);
ALTER TABLE terms ADD COLUMN IF NOT EXISTS source_book        VARCHAR(255);
ALTER TABLE terms ADD COLUMN IF NOT EXISTS source_chapter     VARCHAR(255);
ALTER TABLE terms ADD COLUMN IF NOT EXISTS slug               VARCHAR(120);

-- 2. Migrate data from term_definitions → terms (only while that table still exists)
DO
$$
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_name = 'term_definitions') THEN

            -- Pull one definition per term (prefer ENTRY level; fall back to any)
            UPDATE terms t
            SET casual_definition = td.casual_definition,
                formal_definition = td.formal_definition
            FROM term_definitions td
            WHERE td.term_id = t.id
              AND td.id = (
                  SELECT id FROM term_definitions
                  WHERE term_id = t.id
                  ORDER BY experience_level
                  LIMIT 1
              );

        END IF;
    END
$$;

-- 3. Back-fill slug from name (lower-case, spaces → hyphens, strip non-alphanumeric)
UPDATE terms
SET slug = LOWER(REGEXP_REPLACE(REGEXP_REPLACE(name, '[^a-zA-Z0-9\s-]', '', 'g'), '\s+', '-', 'g'))
WHERE slug IS NULL OR slug = '';

-- 4. Make slug and definitions NOT NULL now that data is populated
ALTER TABLE terms ALTER COLUMN slug               SET NOT NULL;
ALTER TABLE terms ALTER COLUMN casual_definition  SET NOT NULL;
ALTER TABLE terms ALTER COLUMN formal_definition  SET NOT NULL;

-- 5. Restore term_tags keyed off term_id (only if it doesn't exist yet)
CREATE TABLE IF NOT EXISTS term_tags
(
    term_id BIGINT       NOT NULL REFERENCES terms (id) ON DELETE CASCADE,
    tag     VARCHAR(255) NOT NULL
);

-- 6. Migrate tags from term_definition_tags → term_tags
DO
$$
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_name = 'term_definition_tags') THEN

            INSERT INTO term_tags (term_id, tag)
            SELECT DISTINCT td.term_id, tdt.tag
            FROM term_definition_tags tdt
                     JOIN term_definitions td ON td.id = tdt.definition_id
            ON CONFLICT DO NOTHING;

        END IF;
    END
$$;

-- 7. Drop old tables
DROP TABLE IF EXISTS term_definition_tags;
DROP TABLE IF EXISTS term_definitions;

-- 8. Drop old unique constraint on name and add composite unique constraint
ALTER TABLE terms DROP CONSTRAINT IF EXISTS terms_name_key;

-- Unique constraint for book-sourced terms: (name, source_book, source_chapter)
DO
$$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'uq_term_name_book_chapter'
        ) THEN
            ALTER TABLE terms
                ADD CONSTRAINT uq_term_name_book_chapter
                    UNIQUE (name, source_book, source_chapter);
        END IF;
    END
$$;

-- Partial unique index for manual terms (both source fields null): unique by name
DROP INDEX IF EXISTS uq_term_name_manual;
CREATE UNIQUE INDEX uq_term_name_manual
    ON terms (name)
    WHERE source_book IS NULL AND source_chapter IS NULL;
