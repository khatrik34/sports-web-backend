-- Phase 3: scorecards (innings with batting/bowling rows) and ball-by-ball commentary.
-- Both belong to a fixture and are replaced/added by staff. ON DELETE CASCADE keeps a fixture
-- delete from leaving orphan rows behind.

CREATE TABLE cricket_innings (
    id              uuid        NOT NULL,
    match_id        uuid        NOT NULL,
    innings_number  integer     NOT NULL,
    batting_team_id uuid,
    runs            integer     NOT NULL DEFAULT 0,
    wickets         integer     NOT NULL DEFAULT 0,
    overs_text      varchar(16),
    declared        boolean     NOT NULL DEFAULT false,
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now(),
    version         bigint      NOT NULL DEFAULT 0,
    CONSTRAINT pk_cricket_innings PRIMARY KEY (id),
    CONSTRAINT uq_cricket_innings_number UNIQUE (match_id, innings_number),
    CONSTRAINT ck_cricket_innings_runs CHECK (runs >= 0),
    CONSTRAINT ck_cricket_innings_wickets CHECK (wickets >= 0 AND wickets <= 10),
    CONSTRAINT fk_cricket_innings_match FOREIGN KEY (match_id)
        REFERENCES cricket_match (id) ON DELETE CASCADE,
    CONSTRAINT fk_cricket_innings_team FOREIGN KEY (batting_team_id)
        REFERENCES cricket_team (id)
);

CREATE TABLE cricket_batting_entry (
    id         uuid         NOT NULL,
    innings_id uuid         NOT NULL,
    position   integer      NOT NULL DEFAULT 0,
    batter     varchar(120) NOT NULL,
    dismissal  varchar(120),
    runs       integer      NOT NULL DEFAULT 0,
    balls      integer      NOT NULL DEFAULT 0,
    fours      integer      NOT NULL DEFAULT 0,
    sixes      integer      NOT NULL DEFAULT 0,
    not_out    boolean      NOT NULL DEFAULT false,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now(),
    version    bigint       NOT NULL DEFAULT 0,
    CONSTRAINT pk_cricket_batting_entry PRIMARY KEY (id),
    CONSTRAINT ck_cricket_batting_counts CHECK (runs >= 0 AND balls >= 0 AND fours >= 0 AND sixes >= 0),
    CONSTRAINT fk_cricket_batting_innings FOREIGN KEY (innings_id)
        REFERENCES cricket_innings (id) ON DELETE CASCADE
);

CREATE TABLE cricket_bowling_entry (
    id            uuid         NOT NULL,
    innings_id    uuid         NOT NULL,
    position      integer      NOT NULL DEFAULT 0,
    bowler        varchar(120) NOT NULL,
    overs_text    varchar(16),
    maidens       integer      NOT NULL DEFAULT 0,
    runs_conceded integer      NOT NULL DEFAULT 0,
    wickets       integer      NOT NULL DEFAULT 0,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    updated_at    timestamptz  NOT NULL DEFAULT now(),
    version       bigint       NOT NULL DEFAULT 0,
    CONSTRAINT pk_cricket_bowling_entry PRIMARY KEY (id),
    CONSTRAINT ck_cricket_bowling_counts CHECK (
        maidens >= 0 AND runs_conceded >= 0 AND wickets >= 0 AND wickets <= 10),
    CONSTRAINT fk_cricket_bowling_innings FOREIGN KEY (innings_id)
        REFERENCES cricket_innings (id) ON DELETE CASCADE
);

CREATE TABLE cricket_commentary (
    id         uuid        NOT NULL,
    match_id   uuid        NOT NULL,
    over_text  varchar(16),
    kind       varchar(24),
    text       text        NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    version    bigint      NOT NULL DEFAULT 0,
    CONSTRAINT pk_cricket_commentary PRIMARY KEY (id),
    CONSTRAINT fk_cricket_commentary_match FOREIGN KEY (match_id)
        REFERENCES cricket_match (id) ON DELETE CASCADE
);

CREATE INDEX idx_cricket_innings_match ON cricket_innings (match_id);
CREATE INDEX idx_cricket_batting_innings ON cricket_batting_entry (innings_id, position);
CREATE INDEX idx_cricket_bowling_innings ON cricket_bowling_entry (innings_id, position);
CREATE INDEX idx_cricket_commentary_match ON cricket_commentary (match_id, created_at DESC);
