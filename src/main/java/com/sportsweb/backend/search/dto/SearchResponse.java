package com.sportsweb.backend.search.dto;

import java.util.List;

public record SearchResponse(String query, int total, List<SearchResultResponse> results) {
}
