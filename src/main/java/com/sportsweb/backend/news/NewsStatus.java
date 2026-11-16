package com.sportsweb.backend.news;

/**
 * Editorial state. {@code PUBLISHED} with a future {@code publishedAt} is a scheduled
 * article: it is still invisible to the public API until that instant passes.
 */
public enum NewsStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
}
