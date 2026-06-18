-- ============================================================
-- CineVerse Movie Service — PostgreSQL Schema
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ─── Genres ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS genres (
    id   SERIAL      PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(10) NOT NULL UNIQUE
);

INSERT INTO genres (name, slug) VALUES
    ('Action',      'action'),
    ('Comedy',      'comedy'),
    ('Drama',       'drama'),
    ('Horror',      'horror'),
    ('Sci-Fi',      'sci-fi'),
    ('Thriller',    'thriller'),
    ('Romance',     'romance'),
    ('Animation',   'animation'),
    ('Documentary', 'documentary'),
    ('Fantasy',     'fantasy')
ON CONFLICT DO NOTHING;

-- ─── Persons (cast/crew) ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS persons (
    id        UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name      VARCHAR(150) NOT NULL,
    role      VARCHAR(20)  NOT NULL CHECK (role IN ('ACTOR','DIRECTOR','WRITER','PRODUCER')),
    photo_url TEXT
);

CREATE INDEX idx_persons_name ON persons (name);

-- ─── Movies ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS movies (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    title            VARCHAR(255) NOT NULL,
    description      TEXT,
    release_date     DATE,
    content_rating   VARCHAR(10),
    runtime_minutes  INTEGER      CHECK (runtime_minutes > 0),
    average_rating   DECIMAL(3,1) CHECK (average_rating BETWEEN 0 AND 10),
    review_count     INTEGER      NOT NULL DEFAULT 0,
    poster_url       TEXT,
    backdrop_url     TEXT,
    trailer_url      TEXT,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_content_rating CHECK (content_rating IN ('G','PG','PG-13','R','NC-17'))
);

CREATE INDEX idx_movies_title        ON movies (LOWER(title));
CREATE INDEX idx_movies_release_date ON movies (release_date DESC) WHERE active = TRUE;
CREATE INDEX idx_movies_avg_rating   ON movies (average_rating DESC NULLS LAST) WHERE active = TRUE;

-- Full-text search index
CREATE INDEX idx_movies_fts ON movies
    USING GIN (to_tsvector('english', COALESCE(title,'') || ' ' || COALESCE(description,'')));

-- ─── Movie–Genre Junction ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS movie_genres (
    movie_id UUID   NOT NULL REFERENCES movies(id)  ON DELETE CASCADE,
    genre_id INTEGER NOT NULL REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, genre_id)
);

CREATE INDEX idx_movie_genres_genre ON movie_genres (genre_id);

-- ─── Movie–Cast Junction ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS movie_cast (
    movie_id  UUID NOT NULL REFERENCES movies(id)  ON DELETE CASCADE,
    person_id UUID NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, person_id)
);

-- ─── Watchlist ────────────────────────────────────────────────────────────────
-- userId references auth_db users — cross-service, so no FK constraint.
-- Application layer enforces referential integrity.
CREATE TABLE IF NOT EXISTS watchlist (
    id        UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id   UUID        NOT NULL,
    movie_id  UUID        NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    added_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_watchlist_user_movie UNIQUE (user_id, movie_id)
);

CREATE INDEX idx_watchlist_user ON watchlist (user_id);

-- ─── Audit Trigger ────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_movies_updated_at
    BEFORE UPDATE ON movies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ─── Seed Data ────────────────────────────────────────────────────────────────
INSERT INTO persons (id, name, role) VALUES
    (gen_random_uuid(), 'Christopher Nolan',    'DIRECTOR'),
    (gen_random_uuid(), 'Leonardo DiCaprio',    'ACTOR'),
    (gen_random_uuid(), 'Joseph Gordon-Levitt', 'ACTOR')
ON CONFLICT DO NOTHING;

INSERT INTO movies (title, description, release_date, content_rating, runtime_minutes,
                    average_rating, review_count, poster_url)
VALUES (
    'Inception',
    'A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a CEO.',
    '2010-07-16',
    'PG-13',
    148,
    8.8,
    0,
    'https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg'
);
