package com.sportsweb.backend.security.admin.dto;

import jakarta.validation.constraints.Size;

/** Both fields are optional; only non-null values are applied. */
public record UpdateUserRequest(
        @Size(max = 120, message = "must be at most 120 characters")
        String displayName,

        Boolean enabled) {
}
