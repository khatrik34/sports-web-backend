package com.sportsweb.backend.cricket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Create or replace a player profile. */
public record PlayerUpsertRequest(
        String slug,
        @NotBlank @Size(max = 160) String fullName,
        UUID teamId,
        @Size(max = 80) String country,
        @Size(max = 80) String role,
        @Size(max = 80) String battingStyle,
        @Size(max = 80) String bowlingStyle,
        LocalDate bornOn,
        @Size(max = 2048) String imageUrl,
        @Size(max = 4000) String description,
        Boolean active) {
}
