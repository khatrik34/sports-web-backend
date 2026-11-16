package com.sportsweb.backend.news.dto;

/** SEO metadata for a public article page. */
public record PublicSeo(
        String metaTitle,
        String metaDescription,
        String canonicalUrl,
        String robots,
        String keywords,
        String ogTitle,
        String ogDescription,
        String ogImageUrl) {
}
