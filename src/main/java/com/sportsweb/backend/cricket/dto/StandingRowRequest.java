package com.sportsweb.backend.cricket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** One points-table row in a full-table replacement. */
public record StandingRowRequest(
        @NotNull UUID teamId,
        int position,
        int played,
        int won,
        int lost,
        int tied,
        int noResult,
        int points,
        @Size(max = 24) String nrr) {
}
