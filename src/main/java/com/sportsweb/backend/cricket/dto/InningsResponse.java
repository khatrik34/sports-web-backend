package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** One innings with its batting and bowling rows. */
public record InningsResponse(
        UUID id,
        int inningsNumber,
        PublicTeamRef battingTeam,
        int runs,
        int wickets,
        String overs,
        boolean declared,
        List<BattingRowResponse> batting,
        List<BowlingRowResponse> bowling) {
}
