package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Full team representation for the admin panel. */
public record AdminTeamResponse(
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
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        long version) {
}
