package com.sportsweb.backend.videos.dto;

import java.time.Instant;
import java.util.UUID;

/** Full video representation for the admin panel. */
public record AdminVideoResponse(
        UUID id,
        String slug,
        String title,
        String category,
        String description,
        String thumbnailUrl,
        String videoUrl,
        String duration,
        Instant publishedAt,
        boolean visible,
        Instant createdAt,
        Instant updatedAt,
        long version) {
}
