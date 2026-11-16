package com.sportsweb.backend.security.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateUserRequest(
        @NotBlank(message = "is required")
        @Email(message = "must be a valid email address")
        @Size(max = 320, message = "must be at most 320 characters")
        String email,

        @NotBlank(message = "is required")
        @Size(max = 120, message = "must be at most 120 characters")
        String displayName,

        @NotBlank(message = "is required")
        @Size(max = 128, message = "must be at most 128 characters")
        String password,

        @NotEmpty(message = "must contain at least one role")
        Set<String> roleCodes,

        /** Defaults to true: staff created by an administrator must set their own password. */
        Boolean mustChangePassword) {
}
