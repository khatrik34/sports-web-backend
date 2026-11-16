package com.sportsweb.backend.cricket.dto;

import com.sportsweb.backend.cricket.CricketCategory;
import com.sportsweb.backend.cricket.CricketFormat;
import com.sportsweb.backend.cricket.MatchStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Create or replace a fixture. Scores are entered by staff; no live feed is used. */
public record MatchUpsertRequest(
        String slug,
        UUID tournamentId,
        @NotNull UUID team1Id,
        @NotNull UUID team2Id,
        CricketFormat format,
        CricketCategory category,
        @Size(max = 200) String venue,
        @NotNull Instant startTime,
        MatchStatus status,
        @Size(max = 60) String matchNumber,
        @Size(max = 80) String team1Score,
        @Size(max = 80) String team2Score,
        @Size(max = 300) String note,
        @Size(max = 300) String resultText,
        Boolean visible) {
}
