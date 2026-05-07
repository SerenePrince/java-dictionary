-- V1: Initial schema
-- Terms table

CREATE TABLE IF NOT EXISTS terms
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(100)  NOT NULL UNIQUE,
    casual_definition  VARCHAR(500)  NOT NULL,
    formal_definition  VARCHAR(1000) NOT NULL,
    experience_level   VARCHAR(20)   NOT NULL
);

-- Term tags join table

CREATE TABLE IF NOT EXISTS term_tags
(
    term_id BIGINT      NOT NULL REFERENCES terms (id) ON DELETE CASCADE,
    tag     VARCHAR(255) NOT NULL
);
