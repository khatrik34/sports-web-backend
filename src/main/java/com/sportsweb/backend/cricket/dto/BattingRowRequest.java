package com.sportsweb.backend.cricket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Batter row supplied by the admin panel. */
public record BattingRowRequest(
        int position,
        @NotBlank @Size(max = 120) String batter,
        @Size(max = 120) String dismissal,
        int runs,
        int balls,
        int fours,
        int sixes,
        Boolean notOut) {
}
