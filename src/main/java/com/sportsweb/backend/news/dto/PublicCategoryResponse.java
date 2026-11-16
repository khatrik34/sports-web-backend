package com.sportsweb.backend.news.dto;

/**
 * Public category as the website consumes it. The site has no category URL: it filters the
 * news listing with {@code /news?filter=<filter>}, so that is what {@code path} contains.
 */
public record PublicCategoryResponse(
        String slug,
        String name,
        String description,
        String filter,
        String path,
        String url) {
}
