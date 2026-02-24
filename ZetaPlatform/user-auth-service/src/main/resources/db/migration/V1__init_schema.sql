-- =============================================================================
-- V1 — Initial schema for user-auth-service
-- =============================================================================

-- ---------------------------------------------------------------------------
-- auth_users: one row per user holding their credentials
-- ---------------------------------------------------------------------------
CREATE TABLE auth_users (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL UNIQUE,          -- FK to user-mgmt-service.users.id
    username        VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

-- ---------------------------------------------------------------------------
-- service_tokens: encrypted OAuth2 tokens per user / external service
-- ---------------------------------------------------------------------------
CREATE TABLE service_tokens (
    id                       UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                  UUID        NOT NULL,
    service_type             VARCHAR(50) NOT NULL,        -- PEC | SIGN | CONSERVATION
    encrypted_access_token   TEXT        NOT NULL,
    encrypted_refresh_token  TEXT,
    token_expires_at         TIMESTAMPTZ NOT NULL,
    created_at               TIMESTAMP,
    updated_at               TIMESTAMP,

    CONSTRAINT uq_service_tokens_user_service UNIQUE (user_id, service_type)
);
