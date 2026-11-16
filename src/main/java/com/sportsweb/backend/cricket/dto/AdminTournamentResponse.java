package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Full tournament representation, including the teams that take part. */
public record AdminTournamentResponse(
        UUID id,
        String slug,
        String name,
        String shortName,
        String description,
        String format,
        String category,
        String season,
        String host,
        String datesText,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String logoUrl,
        boolean active,
        List<UUID> teamIds,
        List<String> teamSlugs,
        Instant createdAt,
        Instant updatedAt,
        long version) {
}
