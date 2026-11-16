package com.sportsweb.backend.security.admin;

import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.security.admin.dto.CreateUserRequest;
import com.sportsweb.backend.security.admin.dto.SetPasswordRequest;
import com.sportsweb.backend.security.admin.dto.UpdateUserRequest;
import com.sportsweb.backend.security.admin.dto.UpdateUserRolesRequest;
import com.sportsweb.backend.security.admin.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin · Users")
@SecurityRequirement(name = "bearerAuth")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('users:read')")
    @Operation(summary = "List staff accounts")
    public PageResponse<UserResponse> search(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(required = false) String q,
                                             @RequestParam(required = false) Boolean enabled) {
        return userAdminService.search(q, enabled, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('users:read')")
    @Operation(summary = "Get one staff account")
    public UserResponse get(@PathVariable UUID id) {
        return userAdminService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('users:manage')")
    @Operation(summary = "Create a staff account with one or more roles")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userAdminService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/users/" + created.id())).body(created);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('users:manage')")
    @Operation(summary = "Update display name or enabled flag")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return userAdminService.update(id, request);
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('users:manage')")
    @Operation(summary = "Replace the roles assigned to a staff account")
    public UserResponse replaceRoles(@PathVariable UUID id, @Valid @RequestBody UpdateUserRolesRequest request) {
        return userAdminService.replaceRoles(id, request);
    }

    @PostMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:manage')")
    @Operation(summary = "Set a new password for a staff account and revoke its sessions")
    public void resetPassword(@PathVariable UUID id, @Valid @RequestBody SetPasswordRequest request) {
        userAdminService.resetPassword(id, request);
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('users:manage')")
    @Operation(summary = "Clear a lockout caused by failed sign-in attempts")
    public UserResponse unlock(@PathVariable UUID id) {
        return userAdminService.unlock(id);
    }
}
