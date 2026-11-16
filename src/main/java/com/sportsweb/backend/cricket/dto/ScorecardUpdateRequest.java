package com.sportsweb.backend.cricket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Full scorecard replacement for a fixture. */
public record ScorecardUpdateRequest(
        @NotNull @Valid List<InningsRequest> innings) {
}
