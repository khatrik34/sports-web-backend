package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** One points-table row, ordered by position. */
public record PublicStandingRow(
        int position,
        PublicTeamRef team,
        int played,
        int won,
        int lost,
        int tied,
        int noResult,
        int points,
        String nrr) {
}
