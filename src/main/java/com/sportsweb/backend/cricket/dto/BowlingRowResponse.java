package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** One bowler row of a scorecard; economy is a display string. */
public record BowlingRowResponse(
        UUID id,
        int position,
        String bowler,
        String overs,
        int maidens,
        int runsConceded,
        int wickets,
        String economy) {
}
