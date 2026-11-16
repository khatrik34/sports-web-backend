package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Compact team reference embedded in matches, standings and player profiles. */
public record PublicTeamRef(
        UUID id,
        String slug,
        String name,
        String shortName,
        String flag,
        String logoUrl,
        String category,
        String path,
        String url) {
}
