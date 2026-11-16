package com.sportsweb.backend.homepage.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * One block of the homepage configuration. The config is free-form JSON whose shape depends on the
 * type: article slugs for a news rail, match slugs for a match strip, a series slug for a points
 * table, video slugs for a video rail.
 */
public record HomepageSectionRequest(
        String key,
        @NotBlank @Size(max = 40) String type,
        @Size(max = 160) String title,
        int sortOrder,
        Boolean enabled,
        JsonNode config) {
}
