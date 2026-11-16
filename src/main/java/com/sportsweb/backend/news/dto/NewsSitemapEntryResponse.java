package com.sportsweb.backend.news.dto;

import java.time.Instant;

/**
 * Entry of the Google News sitemap feed, matching what the website's
 * {@code app/news-sitemap.xml/route.ts} already renders from its mocks.
 */
public record NewsSitemapEntryResponse(
        String loc,
        String title,
        Instant publishedAt,
        String language) {
}
