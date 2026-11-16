package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A player profile served at /players/{slug}. */
public record PublicPlayer(
        UUID id,
        String slug,
        String name,
        String country,
        String role,
        String battingStyle,
        String bowlingStyle,
        LocalDate bornOn,
        String imageUrl,
        String description,
        PublicTeamRef team,
        String path,
        String url) {
}
