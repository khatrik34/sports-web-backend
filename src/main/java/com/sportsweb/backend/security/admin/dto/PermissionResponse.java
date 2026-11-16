package com.sportsweb.backend.security.admin.dto;

import com.sportsweb.backend.security.user.Permission;

public record PermissionResponse(String code, String description) {

    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(permission.getCode(), permission.getDescription());
    }
}
