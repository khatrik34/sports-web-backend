package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Full fixture representation for the admin panel. */
public record AdminMatchResponse(
        UUID id,
        String slug,
        UUID tournamentId,
        String tournamentName,
        UUID team1Id,
        String team1Name,
        UUID team2Id,
        String team2Name,
        String format,
        String category,
        String venue,
        Instant startTime,
        String status,
        String matchNumber,
        String team1Score,
        String team2Score,
        String note,
        String resultText,
        boolean visible,
        Instant createdAt,
        Instant updatedAt,
        long version) {
}
