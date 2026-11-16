package com.sportsweb.backend.videos;

import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.videos.dto.AdminVideoResponse;
import com.sportsweb.backend.videos.dto.VideoUpsertRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/admin/videos")
@Tag(name = "Admin · Videos")
@SecurityRequirement(name = "bearerAuth")
public class VideoAdminController {

    private final VideoService videoService;

    public VideoAdminController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('videos:read')")
    @Operation(summary = "List videos, including hidden and scheduled ones")
    public PageResponse<AdminVideoResponse> list(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(required = false) String q,
                                                 @RequestParam(required = false) String category,
                                                 @RequestParam(required = false) Boolean visible) {
        return videoService.list(q, category, visible, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('videos:read')")
    @Operation(summary = "Get a video by id")
    public AdminVideoResponse get(@PathVariable UUID id) {
        return videoService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('videos:write')")
    @Operation(summary = "Create a video entry; a future publishedAt schedules it")
    public ResponseEntity<AdminVideoResponse> create(@Valid @RequestBody VideoUpsertRequest request) {
        AdminVideoResponse created = videoService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/videos/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('videos:write')")
    @Operation(summary = "Replace a video's editable fields")
    public AdminVideoResponse update(@PathVariable UUID id, @Valid @RequestBody VideoUpsertRequest request) {
        return videoService.update(id, request);
    }

    @PostMapping("/{id}/show")
    @PreAuthorize("hasAuthority('videos:write')")
    @Operation(summary = "Publish a video to /videos")
    public AdminVideoResponse show(@PathVariable UUID id) {
        return videoService.setVisible(id, true);
    }

    @PostMapping("/{id}/hide")
    @PreAuthorize("hasAuthority('videos:write')")
    @Operation(summary = "Remove a video from /videos without deleting it")
    public AdminVideoResponse hide(@PathVariable UUID id) {
        return videoService.setVisible(id, false);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('videos:write')")
    @Operation(summary = "Delete a video entry permanently (audited)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        videoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
