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
import com.sportsweb.backend.cricket.dto.AdminTeamResponse;
import com.sportsweb.backend.cricket.dto.TeamUpsertRequest;

@RestController
@RequestMapping("/api/v1/admin/teams")
@Tag(name = "Admin · Teams")
@SecurityRequirement(name = "bearerAuth")
public class TeamAdminController {

    private final TeamAdminService service;

    public TeamAdminController(TeamAdminService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cricket:read')")
    @Operation(summary = "List teams, including deactivated ones")
    public PageResponse<AdminTeamResponse> list(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam(required = false) String q,
                                                @RequestParam(required = false) Boolean active) {
        return service.list(q, active, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cricket:read')")
    @Operation(summary = "Get a team by id")
    public AdminTeamResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Create a team; the slug is derived from the name when omitted")
    public ResponseEntity<AdminTeamResponse> create(@Valid @RequestBody TeamUpsertRequest request) {
        AdminTeamResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/teams/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Replace a team's editable fields")
    public AdminTeamResponse update(@PathVariable UUID id, @Valid @RequestBody TeamUpsertRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Make a team visible on the website again")
    public AdminTeamResponse activate(@PathVariable UUID id) {
        return service.setActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Hide a team from the website without deleting its history")
    public AdminTeamResponse deactivate(@PathVariable UUID id) {
        return service.setActive(id, false);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Deactivate a team (teams referenced by matches are never deleted)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.setActive(id, false);
        return ResponseEntity.noContent().build();
    }
}
