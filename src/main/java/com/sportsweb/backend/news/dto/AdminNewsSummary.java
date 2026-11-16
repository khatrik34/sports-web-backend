package com.sportsweb.backend.news.dto;

import com.sportsweb.backend.news.NewsStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminNewsSummary(
        UUID id,
        String slug,
        String title,
        NewsStatus status,
        String categorySlug,
        String categoryName,
        String authorDisplayName,
        boolean featured,
        String path,
        Instant publishedAt,
        Instant updatedAt) {
}
