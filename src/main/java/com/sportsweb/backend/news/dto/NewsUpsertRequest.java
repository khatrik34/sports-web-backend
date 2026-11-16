package com.sportsweb.backend.news.dto;

import com.sportsweb.backend.news.BodyFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Full representation used by both create and update. On update, omitted optional fields are
 * cleared (PUT semantics), so the admin panel should always send the complete form.
 */
public record NewsUpsertRequest(
        @Size(max = 200, message = "must be at most 200 characters")
        String slug,

        @NotBlank(message = "is required")
        @Size(max = 300, message = "must be at most 300 characters")
        String title,

        @Size(max = 400, message = "must be at most 400 characters")
        String subtitle,

        @Size(max = 1000, message = "must be at most 1000 characters")
        String summary,

        @NotBlank(message = "is required")
        String body,

        BodyFormat bodyFormat,

        @Size(max = 120, message = "must be at most 120 characters")
        String categorySlug,

        @Size(max = 120, message = "must be at most 120 characters")
        String authorDisplayName,

        @Size(max = 2048, message = "must be at most 2048 characters")
        String coverImageUrl,

        @Size(max = 300, message = "must be at most 300 characters")
        String coverImageAlt,

        @Size(max = 200, message = "must be at most 200 characters")
        String metaTitle,

        @Size(max = 400, message = "must be at most 400 characters")
        String metaDescription,

        @Size(max = 500, message = "must be at most 500 characters")
        String canonicalPath,

        @Size(max = 200, message = "must be at most 200 characters")
        String ogTitle,

        @Size(max = 400, message = "must be at most 400 characters")
        String ogDescription,

        @Size(max = 2048, message = "must be at most 2048 characters")
        String ogImageUrl,

        @Size(max = 64, message = "must be at most 64 characters")
        String robots,

        @Size(max = 500, message = "must be at most 500 characters")
        String keywords,

        Boolean featured) {
}
