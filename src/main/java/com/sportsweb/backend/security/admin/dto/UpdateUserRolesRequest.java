package com.sportsweb.backend.security.admin.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateUserRolesRequest(
        @NotEmpty(message = "must contain at least one role")
        Set<String> roleCodes) {
}
