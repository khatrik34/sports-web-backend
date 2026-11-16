package com.sportsweb.backend.cricket.admin;

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
import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.cricket.dto.AdminMatchResponse;
import com.sportsweb.backend.cricket.dto.MatchUpsertRequest;

@RestController
@RequestMapping("/api/v1/admin/matches")
@Tag(name = "Admin · Fixtures")
@SecurityRequirement(name = "bearerAuth")
public class MatchAdminController {

    private final MatchAdminService service;

    public MatchAdminController(MatchAdminService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cricket:read')")
    @Operation(summary = "List fixtures, including hidden ones")
    public PageResponse<AdminMatchResponse> list(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String tournament) {
        return service.list(status, tournament, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cricket:read')")
    @Operation(summary = "Get a fixture by id")
    public AdminMatchResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Create a fixture; scores are staff-entered, not fed from an external service")
    public ResponseEntity<AdminMatchResponse> create(@Valid @RequestBody MatchUpsertRequest request) {
        AdminMatchResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/matches/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Replace a fixture, including its scores and state")
    public AdminMatchResponse update(@PathVariable UUID id, @Valid @RequestBody MatchUpsertRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/show")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Publish a fixture to the website")
    public AdminMatchResponse show(@PathVariable UUID id) {
        return service.setVisible(id, true);
    }

    @PostMapping("/{id}/hide")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Remove a fixture from the website without deleting it")
    public AdminMatchResponse hide(@PathVariable UUID id) {
        return service.setVisible(id, false);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Delete a fixture permanently (audited)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
