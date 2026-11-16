package com.sportsweb.backend.cricket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Innings supplied by the admin panel; replaces the stored innings on PUT. */
public record InningsRequest(
        int inningsNumber,
        UUID battingTeamId,
        int runs,
        int wickets,
        @Size(max = 16) String overs,
        Boolean declared,
        @Valid List<BattingRowRequest> batting,
        @Valid List<BowlingRowRequest> bowling) {
}
