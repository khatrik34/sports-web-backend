package com.sportsweb.backend.search.dto;

import java.util.UUID;

/** One search hit. {@code type} is one of news, team, player, match, series, video. */
public record SearchResultResponse(
        String type,
        UUID id,
        String title,
        String subtitle,
        String path,
        String url) {
}
