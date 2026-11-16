package com.sportsweb.backend.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryUpsertRequest(
        @Size(max = 120, message = "must be at most 120 characters")
        String slug,

        @NotBlank(message = "is required")
        @Size(max = 120, message = "must be at most 120 characters")
        String name,

        @Size(max = 500, message = "must be at most 500 characters")
        String description,

        Integer sortOrder) {
}
