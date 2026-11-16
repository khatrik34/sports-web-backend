package com.sportsweb.backend.security.admin;

import com.sportsweb.backend.security.admin.dto.PermissionResponse;
import com.sportsweb.backend.security.admin.dto.RoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin · Roles")
@SecurityRequirement(name = "bearerAuth")
public class RoleAdminController {

    private final UserAdminService userAdminService;

    public RoleAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('roles:read')")
    @Operation(summary = "List roles with their permissions")
    public List<RoleResponse> roles() {
        return userAdminService.roles();
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('roles:read')")
    @Operation(summary = "List all permissions")
    public List<PermissionResponse> permissions() {
        return userAdminService.permissions();
    }
}
