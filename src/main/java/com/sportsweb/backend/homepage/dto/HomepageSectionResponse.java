package com.sportsweb.backend.homepage.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record HomepageSectionResponse(
        String key,
        String type,
        String title,
        int sortOrder,
        boolean enabled,
        JsonNode config) {
}
