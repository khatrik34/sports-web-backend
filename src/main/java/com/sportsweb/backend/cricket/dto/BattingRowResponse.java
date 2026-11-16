package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** One batter row of a scorecard. */
public record BattingRowResponse(
        UUID id,
        int position,
        String batter,
        String dismissal,
        int runs,
        int balls,
        int fours,
        int sixes,
        boolean notOut) {
}
