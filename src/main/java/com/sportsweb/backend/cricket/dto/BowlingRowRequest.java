package com.sportsweb.backend.cricket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Bowler row supplied by the admin panel. */
public record BowlingRowRequest(
        int position,
        @NotBlank @Size(max = 120) String bowler,
        @Size(max = 16) String overs,
        int maidens,
        int runsConceded,
        int wickets) {
}
