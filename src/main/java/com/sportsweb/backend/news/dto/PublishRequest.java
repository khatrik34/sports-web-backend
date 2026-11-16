package com.sportsweb.backend.news.dto;

import java.time.Instant;

/**
 * Optional {@code publishedAt}: omit to publish now, or pass a future instant to schedule.
 * A scheduled article stays invisible to the public API until that instant.
 */
public record PublishRequest(Instant publishedAt) {
}
