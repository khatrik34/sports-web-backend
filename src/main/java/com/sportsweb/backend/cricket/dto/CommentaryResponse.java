package com.sportsweb.backend.cricket.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** One ball-by-ball entry. */
public record CommentaryResponse(
        UUID id,
        String over,
        String kind,
        String text,
        Instant createdAt) {
}
