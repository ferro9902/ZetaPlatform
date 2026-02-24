-- =============================================================================
-- V1 — Initial schema for user-mgmt-service
-- =============================================================================

-- ---------------------------------------------------------------------------
-- users: canonical user profiles managed by this service
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id                        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    username                  VARCHAR(255) NOT NULL UNIQUE,
    email                     VARCHAR(255) NOT NULL UNIQUE,
    first_name                VARCHAR(255) NOT NULL,
    last_name                 VARCHAR(255) NOT NULL,
    active                    BOOLEAN      NOT NULL DEFAULT TRUE,
    role                      VARCHAR(255) NOT NULL DEFAULT 'ROLE_USER',
    semantic_indexing_enabled BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at                TIMESTAMP,
    updated_at                TIMESTAMP
);
