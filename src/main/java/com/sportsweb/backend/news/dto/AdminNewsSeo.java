package com.sportsweb.backend.news.dto;

public record AdminNewsSeo(
        String metaTitle,
        String metaDescription,
        String canonicalPath,
        String canonicalUrl,
        String ogTitle,
        String ogDescription,
        String ogImageUrl,
        String robots,
        String keywords) {
}
