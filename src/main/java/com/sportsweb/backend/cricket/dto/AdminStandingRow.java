package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Points-table row in the admin representation (team ids included). */
public record AdminStandingRow(
        UUID teamId,
        String teamSlug,
        String teamName,
        int position,
        int played,
        int won,
        int lost,
        int tied,
        int noResult,
        int points,
        String nrr) {
}
