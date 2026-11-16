-- Phase 3: videos. Embed-only (YouTube/Vimeo/MP4 URLs); no video files are stored by this service.

CREATE TABLE video (
    id            uuid         NOT NULL,
    slug          varchar(200) NOT NULL,
    title         varchar(300) NOT NULL,
    description   text,
    category      varchar(80),
    thumbnail_url varchar(2048),
    video_url     varchar(2048) NOT NULL,
    duration_text varchar(16),
    published_at  timestamptz,
    visible       boolean      NOT NULL DEFAULT true,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    updated_at    timestamptz  NOT NULL DEFAULT now(),
    version       bigint       NOT NULL DEFAULT 0,
    CONSTRAINT pk_video PRIMARY KEY (id),
    CONSTRAINT uq_video_slug UNIQUE (slug)
);

CREATE INDEX idx_video_published_at ON video (published_at);
CREATE INDEX idx_video_visible ON video (visible);
CREATE INDEX idx_video_category ON video (category);
