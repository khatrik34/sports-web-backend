package com.sportsweb.backend.media.dto;

import java.time.Instant;
import java.util.UUID;

public record MediaResponse(
        UUID id,
        String url,
        String filename,
        String contentType,
        Long sizeBytes,
        Integer width,
        Integer height,
        String altText,
        String caption,
        String credit,
        Instant createdAt,
        Instant updatedAt,
        long version) {
}
