package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A series or tournament served at /cricket/series and /cricket/series/{slug}. */
public record PublicTournament(
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
        List<String> teamSlugs,
        String path,
        String url) {
}
