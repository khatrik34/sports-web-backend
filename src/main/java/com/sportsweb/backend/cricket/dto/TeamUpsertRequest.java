package com.sportsweb.backend.cricket.dto;

import com.sportsweb.backend.cricket.CricketCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Create or replace a team. Slug is optional and derived from the name when absent. */
public record TeamUpsertRequest(
        String slug,
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 24) String shortName,
        CricketCategory category,
        @Size(max = 8) String countryCode,
        @Size(max = 32) String flag,
        @Size(max = 32) String sport,
        @Size(max = 4000) String description,
        @Size(max = 2048) String logoUrl,
        Boolean active) {
}
