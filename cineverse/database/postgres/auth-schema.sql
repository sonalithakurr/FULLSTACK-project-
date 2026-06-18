-- ============================================================
-- CineVerse Auth Service — PostgreSQL Schema
-- ============================================================
-- Design principles:
--   - UUID primary keys (no sequential enumeration)
--   - Timestamps using TIMESTAMPTZ (timezone-aware)
--   - Partial indexes for active users (performance)
--   - Constraints enforce data integrity at DB level (defense in depth)
-- ============================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ─── Users ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username       VARCHAR(50)  NOT NULL,
    email          VARCHAR(255) NOT NULL,
    password_hash  TEXT         NOT NULL,
    display_name   VARCHAR(100) NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT ck_email_format   CHECK (email ~* '^[^@]+@[^@]+\.[^@]+$'),
    CONSTRAINT ck_username_chars CHECK (username ~ '^[a-zA-Z0-9_]+$')
);

-- Only index active users — most queries filter by active=true
CREATE INDEX idx_users_email_active    ON users (email)    WHERE active = TRUE;
CREATE INDEX idx_users_username_active ON users (username) WHERE active = TRUE;
CREATE INDEX idx_users_created_at      ON users (created_at DESC);

-- ─── User Roles ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT ck_valid_role CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MODERATOR'))
);

-- ─── Audit Trigger ────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ─── Seed Data ────────────────────────────────────────────────────────────────
-- Admin user (password: Admin@123 — change in production!)
INSERT INTO users (id, username, email, password_hash, display_name, active, email_verified)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@cineverse.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4tHKqB3m6K',
    'CineVerse Admin',
    TRUE,
    TRUE
) ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE username = 'admin'
ON CONFLICT DO NOTHING;
