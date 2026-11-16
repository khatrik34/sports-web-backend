package com.sportsweb.backend.cricket.dto;

import java.util.List;
import java.util.UUID;

/**
 * Backs the website's /rankings page, which renders the same table as a series points table.
 * The tournament chosen is the ongoing one for that format, or the most recently started one.
 */
public record PublicRankings(
        UUID tournamentId,
        String tournamentName,
        String tournamentSlug,
        String format,
        String category,
        String status,
        List<PublicStandingRow> rows) {
}
