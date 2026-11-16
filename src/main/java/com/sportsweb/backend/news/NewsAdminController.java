package com.sportsweb.backend.news;

import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.news.dto.AdminNewsDetail;
import com.sportsweb.backend.news.dto.AdminNewsSummary;
import com.sportsweb.backend.news.dto.NewsUpsertRequest;
import com.sportsweb.backend.news.dto.PublishRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/news")
@Tag(name = "Admin · News")
@SecurityRequirement(name = "bearerAuth")
public class NewsAdminController {

    private final NewsAdminService newsAdminService;

    public NewsAdminController(NewsAdminService newsAdminService) {
        this.newsAdminService = newsAdminService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('news:read')")
    @Operation(summary = "List articles in any state (drafts included)")
    public PageResponse<AdminNewsSummary> list(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(required = false) String status,
                                               @RequestParam(required = false) String category,
                                               @RequestParam(required = false) String q) {
        return newsAdminService.list(status, category, q, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('news:read')")
    @Operation(summary = "Get one article including drafts")
    public AdminNewsDetail get(@PathVariable UUID id) {
        return newsAdminService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('news:write')")
    @Operation(summary = "Create a draft article")
    public ResponseEntity<AdminNewsDetail> create(@Valid @RequestBody NewsUpsertRequest request) {
        AdminNewsDetail created = newsAdminService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/news/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('news:write')")
    @Operation(summary = "Replace an article's editable content (full representation)")
    public AdminNewsDetail update(@PathVariable UUID id, @Valid @RequestBody NewsUpsertRequest request) {
        return newsAdminService.update(id, request);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('news:publish')")
    @Operation(summary = "Publish now, or schedule with a future publishedAt")
    public AdminNewsDetail publish(@PathVariable UUID id,
                                   @RequestBody(required = false) PublishRequest request) {
        return newsAdminService.publish(id, request);
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('news:publish')")
    @Operation(summary = "Return a published article to draft and remove it from public APIs")
    public AdminNewsDetail unpublish(@PathVariable UUID id) {
        return newsAdminService.unpublish(id);
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('news:write')")
    @Operation(summary = "Archive an article; archived articles are never public")
    public AdminNewsDetail archive(@PathVariable UUID id) {
        return newsAdminService.archive(id);
    }
}
