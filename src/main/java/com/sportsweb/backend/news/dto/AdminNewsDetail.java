package com.sportsweb.backend.news.dto;

import com.sportsweb.backend.news.BodyFormat;
import com.sportsweb.backend.news.NewsStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminNewsDetail(
        UUID id,
        String slug,
        String title,
        String subtitle,
        String summary,
        String body,
        BodyFormat bodyFormat,
        NewsStatus status,
        String categorySlug,
        String categoryName,
        String authorDisplayName,
        String coverImageUrl,
        String coverImageAlt,
        boolean featured,
        String path,
        String url,
        Instant publishedAt,
        Instant archivedAt,
        Instant createdAt,
        Instant updatedAt,
        long version,
        AdminNewsSeo seo) {
}
