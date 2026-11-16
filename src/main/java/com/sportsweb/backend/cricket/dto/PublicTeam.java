package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A team as served to the website at /teams and /teams/{slug}. */
public record PublicTeam(
        UUID id,
        String slug,
        String name,
        String shortName,
        String category,
        String countryCode,
        String flag,
        String sport,
        String description,
        String logoUrl,
        String path,
        String url) {
}
