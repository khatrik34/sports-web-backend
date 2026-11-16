package com.sportsweb.backend.news;

import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.news.dto.PublicCategoryResponse;
import com.sportsweb.backend.news.dto.PublicNewsDetail;
import com.sportsweb.backend.news.dto.PublicNewsSummary;
import com.sportsweb.backend.news.dto.SitemapEntryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Anonymous, read-only API used by the public website. Only published content is reachable;
 * drafts and any admin-only field are impossible to obtain from these endpoints.
 */
@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public · News")
public class NewsPublicController {

    private final NewsPublicService newsPublicService;

    public NewsPublicController(NewsPublicService newsPublicService) {
        this.newsPublicService = newsPublicService;
    }

    @GetMapping("/news")
    @Operation(summary = "Published articles, newest first")
    public PageResponse<PublicNewsSummary> list(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam(required = false) String category,
                                                @RequestParam(required = false) String q) {
        return newsPublicService.list(category, q, page, size);
    }

    @GetMapping("/news/latest")
    @Operation(summary = "Latest published articles for homepage rails")
    public List<PublicNewsSummary> latest(@RequestParam(defaultValue = "10") int limit,
                                          @RequestParam(defaultValue = "false") boolean featured) {
        return newsPublicService.latest(limit, featured);
    }

    @GetMapping("/news/{slug}")
    @Operation(summary = "One published article by slug (404 for drafts, archived or scheduled)")
    public PublicNewsDetail bySlug(@PathVariable String slug) {
        return newsPublicService.getBySlug(slug);
    }

    @GetMapping("/categories")
    @Operation(summary = "Public categories")
    public List<PublicCategoryResponse> categories() {
        return newsPublicService.categories();
    }

    @GetMapping("/sitemap")
    @Operation(summary = "Sitemap entries for published articles and categories")
    public List<SitemapEntryResponse> sitemap() {
        return newsPublicService.sitemap();
    }
}
