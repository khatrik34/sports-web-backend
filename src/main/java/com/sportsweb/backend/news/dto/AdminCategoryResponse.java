package com.sportsweb.backend.news.dto;

import java.util.UUID;

public record AdminCategoryResponse(
        UUID id,
        String slug,
        String name,
        String description,
        int sortOrder,
        String path,
        long version) {
}
