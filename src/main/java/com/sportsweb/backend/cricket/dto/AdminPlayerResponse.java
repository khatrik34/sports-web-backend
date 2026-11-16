package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Full player representation for the admin panel. */
public record AdminPlayerResponse(
        UUID id,
        String slug,
        String name,
        UUID teamId,
        String teamSlug,
        String teamName,
        String country,
        String role,
        String battingStyle,
        String bowlingStyle,
        LocalDate bornOn,
        String imageUrl,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        long version) {
}
