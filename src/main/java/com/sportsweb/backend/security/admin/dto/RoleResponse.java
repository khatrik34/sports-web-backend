package com.sportsweb.backend.security.admin.dto;

import com.sportsweb.backend.security.user.Role;

import java.util.List;

public record RoleResponse(
        String code,
        String name,
        String description,
        boolean systemRole,
        List<String> permissions) {

    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isSystemRole(),
                List.copyOf(role.permissionCodes()));
    }
}
