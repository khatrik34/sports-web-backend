package com.sportsweb.backend.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Register a media asset that is already hosted (CDN, S3, R2). This service stores metadata and the
 * URL; it deliberately does not accept binary uploads in this phase.
 */
public record MediaRequest(
        @NotBlank @Size(max = 2048) String url,
        @Size(max = 255) String filename,
        @Size(max = 120) String contentType,
        @PositiveOrZero Long sizeBytes,
        @PositiveOrZero Integer width,
        @PositiveOrZero Integer height,
        @Size(max = 300) String altText,
        @Size(max = 300) String caption,
        @Size(max = 160) String credit) {
}
