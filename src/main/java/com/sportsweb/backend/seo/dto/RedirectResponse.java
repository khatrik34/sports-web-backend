package com.sportsweb.backend.seo.dto;

import java.time.Instant;
import java.util.UUID;

public record RedirectResponse(
        UUID id,
        String fromPath,
        String toPath,
        int statusCode,
        String note,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt,
        long version) {
}
