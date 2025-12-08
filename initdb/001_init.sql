-- Initial schema for Morpholoom
-- Applies to PostgreSQL

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    username        VARCHAR(100) NOT NULL,
    social_provider VARCHAR(50) DEFAULT 'NONE',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS images (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_url   TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS videos (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_url   TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS creations (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       TEXT NOT NULL,
    image_url   TEXT,
    likes       INT NOT NULL DEFAULT 0,
    rank_score  DOUBLE PRECISION NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS creation_likes (
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    creation_id BIGINT NOT NULL REFERENCES creations(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, creation_id)
);

CREATE INDEX IF NOT EXISTS idx_creations_created_at ON creations (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_creations_likes_score ON creations (likes DESC, rank_score DESC);
CREATE INDEX IF NOT EXISTS idx_images_user ON images (user_id);
CREATE INDEX IF NOT EXISTS idx_videos_user ON videos (user_id);

