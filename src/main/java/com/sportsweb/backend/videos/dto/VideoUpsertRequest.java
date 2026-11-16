package com.sportsweb.backend.videos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Create or replace a video. {@code publishedAt} in the future schedules it, and a missing value
 * keeps it out of the public API even when visible is true.
 */
public record VideoUpsertRequest(
        String slug,
        @NotBlank @Size(max = 300) String title,
        @Size(max = 80) String category,
        @Size(max = 4000) String description,
        @Size(max = 2048) String thumbnailUrl,
        @NotBlank @Size(max = 2048) String videoUrl,
        @Size(max = 16) String duration,
        Instant publishedAt,
        Boolean visible) {
}
