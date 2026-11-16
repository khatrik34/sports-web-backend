package com.sportsweb.backend.seo;

import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.seo.dto.RedirectRequest;
import com.sportsweb.backend.seo.dto.RedirectResponse;
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
@RequestMapping("/api/v1/admin/redirects")
@Tag(name = "Admin · Redirects")
@SecurityRequirement(name = "bearerAuth")
public class RedirectAdminController {

    private final RedirectService redirectService;

    public RedirectAdminController(RedirectService redirectService) {
        this.redirectService = redirectService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('seo:read')")
    @Operation(summary = "List redirects")
    public PageResponse<RedirectResponse> list(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(required = false) String q,
                                               @RequestParam(required = false) Boolean enabled) {
        return redirectService.list(q, enabled, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('seo:read')")
    @Operation(summary = "Get one redirect")
    public RedirectResponse get(@PathVariable UUID id) {
        return redirectService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('seo:write')")
    @Operation(summary = "Create a redirect (301/302/307/308) or retire a path (410)")
    public ResponseEntity<RedirectResponse> create(@Valid @RequestBody RedirectRequest request) {
        RedirectResponse created = redirectService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/redirects/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('seo:write')")
    @Operation(summary = "Replace a redirect")
    public RedirectResponse update(@PathVariable UUID id, @Valid @RequestBody RedirectRequest request) {
        return redirectService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('seo:write')")
    @Operation(summary = "Delete a redirect")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        redirectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
