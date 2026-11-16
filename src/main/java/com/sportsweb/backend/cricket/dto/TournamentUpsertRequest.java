package com.sportsweb.backend.cricket.dto;

import com.sportsweb.backend.cricket.CricketCategory;
import com.sportsweb.backend.cricket.CricketFormat;
import com.sportsweb.backend.cricket.TournamentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Create or replace a series or tournament, optionally assigning teams. */
public record TournamentUpsertRequest(
        String slug,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 60) String shortName,
        @Size(max = 4000) String description,
        CricketFormat format,
        CricketCategory category,
        @Size(max = 40) String season,
        @Size(max = 160) String host,
        @Size(max = 120) String datesText,
        LocalDate startDate,
        LocalDate endDate,
        TournamentStatus status,
        @Size(max = 2048) String logoUrl,
        Boolean active,
        @Size(max = 40) List<UUID> teamIds) {
}
