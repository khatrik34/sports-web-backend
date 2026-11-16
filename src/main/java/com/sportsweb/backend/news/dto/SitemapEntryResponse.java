package com.sportsweb.backend.news.dto;

import java.time.Instant;

/** One entry of the main sitemap feed. Static site paths stay owned by the website. */
public record SitemapEntryResponse(
        String loc,
        Instant lastModified,
        String changeFrequency,
        double priority) {
}
