package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * A fixture or result served at /cricket/match/{slug} and the fixture/live listings.
 *
 * <p>{@code status} is the phase the website renders ({@code live}, {@code upcoming},
 * {@code recent}); {@code state} is the stored editorial state ({@code SCHEDULED},
 * {@code LIVE}, {@code COMPLETED}, {@code ABANDONED}).</p>
 */
public record PublicMatch(
        UUID id,
        String slug,
        String tournamentName,
        String tournamentSlug,
        String format,
        String category,
        String venue,
        Instant startTime,
        String status,
        String state,
        String matchNumber,
        PublicTeamRef team1,
        PublicTeamRef team2,
        String team1Score,
        String team2Score,
        String note,
        String resultText,
        String path,
        String url) {
}
