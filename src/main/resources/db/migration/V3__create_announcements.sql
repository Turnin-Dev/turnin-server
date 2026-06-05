CREATE TYPE announcement_audience AS ENUM ('all', 'premium', 'admin');
CREATE TYPE announcement_status AS ENUM ('active', 'inactive');

CREATE TABLE announcement (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(100)          NOT NULL,
    content         TEXT                  NOT NULL,
    target_audience announcement_audience NOT NULL DEFAULT 'all',
    status          announcement_status   NOT NULL DEFAULT 'inactive',
    expires_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ           NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ           NOT NULL DEFAULT now()
);

CREATE TABLE announcement_read (
    announcement_id BIGINT      NOT NULL REFERENCES announcement(id) ON DELETE CASCADE,
    user_id         BIGINT      NOT NULL REFERENCES "user"(id)       ON DELETE CASCADE,
    read_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (announcement_id, user_id)
);

CREATE INDEX idx_announcement_status_expires
    ON announcement(status, expires_at);

CREATE INDEX idx_announcement_read_user
    ON announcement_read(user_id, announcement_id);