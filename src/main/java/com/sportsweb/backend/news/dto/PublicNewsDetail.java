package com.sportsweb.backend.news.dto;

import com.sportsweb.backend.news.BodyFormat;

import java.time.Instant;

/** Public article page payload, including everything needed to render meta tags. */
public record PublicNewsDetail(
        String slug,
        String title,
        String subtitle,
        String summary,
        String body,
        BodyFormat bodyFormat,
        String categorySlug,
        String categoryName,
        String authorDisplayName,
        String coverImageUrl,
        String coverImageAlt,
        boolean featured,
        Instant publishedAt,
        Instant updatedAt,
        String path,
        String url,
        PublicSeo seo) {
}
