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
import com.sportsweb.backend.cricket.dto.AdminPlayerResponse;
import com.sportsweb.backend.cricket.dto.PlayerUpsertRequest;

@RestController
@RequestMapping("/api/v1/admin/players")
@Tag(name = "Admin · Players")
@SecurityRequirement(name = "bearerAuth")
public class PlayerAdminController {

    private final PlayerAdminService service;

    public PlayerAdminController(PlayerAdminService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cricket:read')")
    @Operation(summary = "List players, including deactivated ones")
    public PageResponse<AdminPlayerResponse> list(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  @RequestParam(required = false) String q,
                                                  @RequestParam(required = false) Boolean active,
                                                  @RequestParam(required = false) String team) {
        return service.list(q, active, team, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cricket:read')")
    @Operation(summary = "Get a player by id")
    public AdminPlayerResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Create a player profile")
    public ResponseEntity<AdminPlayerResponse> create(@Valid @RequestBody PlayerUpsertRequest request) {
        AdminPlayerResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/players/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Replace a player's editable fields")
    public AdminPlayerResponse update(@PathVariable UUID id, @Valid @RequestBody PlayerUpsertRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Activate a player profile")
    public AdminPlayerResponse activate(@PathVariable UUID id) {
        return service.setActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Deactivate a player profile")
    public AdminPlayerResponse deactivate(@PathVariable UUID id) {
        return service.setActive(id, false);
    }
}
