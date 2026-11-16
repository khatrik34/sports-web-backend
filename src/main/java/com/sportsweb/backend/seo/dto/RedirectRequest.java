package com.sportsweb.backend.seo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Create or replace a redirect. The status code must be 301, 302, 307 or 308 (a redirect, so a
 * target path is required) or 410 (gone, so the target must be empty).
 */
public record RedirectRequest(
        @NotBlank @Size(max = 500) String fromPath,
        @Size(max = 500) String toPath,
        Integer statusCode,
        @Size(max = 300) String note,
        Boolean enabled) {
}
