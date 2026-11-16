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
import com.sportsweb.backend.cricket.dto.AdminStandingRow;
import com.sportsweb.backend.cricket.dto.AdminTournamentResponse;
import com.sportsweb.backend.cricket.dto.StandingsUpdateRequest;
import com.sportsweb.backend.cricket.dto.TournamentUpsertRequest;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/tournaments")
@Tag(name = "Admin · Series")
@SecurityRequirement(name = "bearerAuth")
public class TournamentAdminController {

    private final TournamentAdminService service;

    public TournamentAdminController(TournamentAdminService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cricket:read')")
    @Operation(summary = "List series, including deactivated ones")
    public PageResponse<AdminTournamentResponse> list(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size,
                                                      @RequestParam(required = false) String q,
                                                      @RequestParam(required = false) Boolean active) {
        return service.list(q, active, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cricket:read')")
    @Operation(summary = "Get a series by id")
    public AdminTournamentResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Create a series, optionally with its participating teams")
    public ResponseEntity<AdminTournamentResponse> create(@Valid @RequestBody TournamentUpsertRequest request) {
        AdminTournamentResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/tournaments/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Replace a series and its team list")
    public AdminTournamentResponse update(@PathVariable UUID id, @Valid @RequestBody TournamentUpsertRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Activate a series page")
    public AdminTournamentResponse activate(@PathVariable UUID id) {
        return service.setActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Deactivate a series page")
    public AdminTournamentResponse deactivate(@PathVariable UUID id) {
        return service.setActive(id, false);
    }

    @GetMapping("/{id}/standings")
    @PreAuthorize("hasAuthority('cricket:read')")
    @Operation(summary = "Read the points table")
    public List<AdminStandingRow> standings(@PathVariable UUID id) {
        return service.standings(id);
    }

    @PutMapping("/{id}/standings")
    @PreAuthorize("hasAuthority('cricket:write')")
    @Operation(summary = "Replace the whole points table (atomic)")
    public List<AdminStandingRow> replaceStandings(@PathVariable UUID id,
                                                  @Valid @RequestBody StandingsUpdateRequest request) {
        return service.replaceStandings(id, request);
    }
}
