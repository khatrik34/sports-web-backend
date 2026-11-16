package com.sportsweb.backend.news.dto;

import java.time.Instant;

/** One entry of the sitemap feed the website can render as sitemap.xml. */
public record SitemapEntryResponse(
        String loc,
        Instant lastModified,
        String changeFrequency,
        double priority) {
}
