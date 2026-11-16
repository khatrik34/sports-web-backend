-- Phase 3: SEO redirects, homepage configuration and media metadata.

CREATE TABLE seo_redirect (
    id          uuid         NOT NULL,
    from_path   varchar(500) NOT NULL,
    to_path     varchar(500),
    status_code integer      NOT NULL DEFAULT 301,
    note        varchar(300),
    enabled     boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    version     bigint       NOT NULL DEFAULT 0,
    CONSTRAINT pk_seo_redirect PRIMARY KEY (id),
    CONSTRAINT uq_seo_redirect_from UNIQUE (from_path),
    CONSTRAINT ck_seo_redirect_status CHECK (status_code IN (301, 302, 307, 308, 410)),
    CONSTRAINT ck_seo_redirect_target CHECK (status_code = 410 OR to_path IS NOT NULL)
);

CREATE INDEX idx_seo_redirect_enabled ON seo_redirect (enabled);

CREATE TABLE homepage_section (
    id           uuid        NOT NULL,
    section_key  varchar(60) NOT NULL,
    type         varchar(40) NOT NULL,
    title        varchar(160),
    sort_order   integer     NOT NULL DEFAULT 0,
    enabled      boolean     NOT NULL DEFAULT true,
    config_json  text        NOT NULL DEFAULT '{}',
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now(),
    version      bigint      NOT NULL DEFAULT 0,
    CONSTRAINT pk_homepage_section PRIMARY KEY (id),
    CONSTRAINT uq_homepage_section_key UNIQUE (section_key)
);

CREATE INDEX idx_homepage_section_order ON homepage_section (sort_order);

CREATE TABLE media_asset (
    id           uuid          NOT NULL,
    url          varchar(2048) NOT NULL,
    filename     varchar(255),
    content_type varchar(120),
    size_bytes   bigint,
    width        integer,
    height       integer,
    alt_text     varchar(300),
    caption      varchar(300),
    credit       varchar(160),
    created_at   timestamptz   NOT NULL DEFAULT now(),
    updated_at   timestamptz   NOT NULL DEFAULT now(),
    version      bigint        NOT NULL DEFAULT 0,
    CONSTRAINT pk_media_asset PRIMARY KEY (id),
    CONSTRAINT ck_media_asset_size CHECK (size_bytes IS NULL OR size_bytes >= 0),
    CONSTRAINT ck_media_asset_dimensions CHECK (
        (width IS NULL OR width >= 0) AND (height IS NULL OR height >= 0))
);

CREATE INDEX idx_media_asset_created ON media_asset (created_at DESC);
