package com.sportsweb.backend.videos.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Video as served to the website. Field names match the site's own vocabulary where it differs:
 * {@code image} is the thumbnail, {@code duration} is the display string ("08:24").
 */
public record PublicVideo(
        UUID id,
        String slug,
        String title,
        String category,
        String image,
        String duration,
        String description,
        String videoUrl,
        Instant published,
        String path,
        String url) {
}
