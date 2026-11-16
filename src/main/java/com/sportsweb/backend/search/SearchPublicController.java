package com.sportsweb.backend.search;

import com.sportsweb.backend.search.dto.SearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/search")
@Tag(name = "Public · Search")
public class SearchPublicController {

    private final SearchService searchService;

    public SearchPublicController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    @Operation(summary = "Search news, fixtures, series, teams, players and videos in one call")
    public SearchResponse search(@RequestParam String q,
                                 @RequestParam(defaultValue = "5") int limit) {
        return searchService.search(q, limit);
    }
}
