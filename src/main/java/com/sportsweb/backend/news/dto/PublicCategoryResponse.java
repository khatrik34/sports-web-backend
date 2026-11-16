package com.sportsweb.backend.news.dto;

public record PublicCategoryResponse(
        String slug,
        String name,
        String description,
        String path,
        String url) {
}
