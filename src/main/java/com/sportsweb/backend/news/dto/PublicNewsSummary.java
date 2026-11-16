package com.sportsweb.backend.news.dto;

import java.time.Instant;

/** Public list item. Contains no editorial or internal identifiers. */
public record PublicNewsSummary(
        String slug,
        String title,
        String subtitle,
        String summary,
        String categorySlug,
        String categoryName,
        String authorDisplayName,
        String coverImageUrl,
        String coverImageAlt,
        boolean featured,
        Instant publishedAt,
        Instant updatedAt,
        String path,
        String url) {
}
