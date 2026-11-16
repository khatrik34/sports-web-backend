package com.sportsweb.backend.media;

import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.media.dto.MediaRequest;
import com.sportsweb.backend.media.dto.MediaResponse;
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

/**
 * Media metadata. Files are hosted elsewhere (CDN/S3); this API registers and describes them so
 * editors can reuse them with consistent alt text and credits.
 */
@RestController
@RequestMapping("/api/v1/admin/media")
@Tag(name = "Admin · Media")
@SecurityRequirement(name = "bearerAuth")
public class MediaAdminController {

    private final MediaService mediaService;

    public MediaAdminController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('media:read')")
    @Operation(summary = "List registered media")
    public PageResponse<MediaResponse> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) String q) {
        return mediaService.list(q, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('media:read')")
    @Operation(summary = "Get one media asset")
    public MediaResponse get(@PathVariable UUID id) {
        return mediaService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('media:write')")
    @Operation(summary = "Register an already hosted asset by URL (no binary upload in this phase)")
    public ResponseEntity<MediaResponse> create(@Valid @RequestBody MediaRequest request) {
        MediaResponse created = mediaService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/media/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('media:write')")
    @Operation(summary = "Update an asset's metadata")
    public MediaResponse update(@PathVariable UUID id, @Valid @RequestBody MediaRequest request) {
        return mediaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('media:write')")
    @Operation(summary = "Remove an asset registration (the hosted file is untouched)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        mediaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
