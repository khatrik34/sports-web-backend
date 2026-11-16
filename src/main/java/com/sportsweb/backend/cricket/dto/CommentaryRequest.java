package com.sportsweb.backend.cricket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A new ball-by-ball entry. */
public record CommentaryRequest(
        @Size(max = 16) String over,
        @Size(max = 24) String kind,
        @NotBlank @Size(max = 2000) String text) {
}
