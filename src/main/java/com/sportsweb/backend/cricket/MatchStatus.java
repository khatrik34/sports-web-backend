package com.sportsweb.backend.cricket;

/** Editorial state of a fixture. The website phase (live/upcoming/recent) is derived from it. */
public enum MatchStatus {

    SCHEDULED,
    LIVE,
    COMPLETED,
    ABANDONED;
}
