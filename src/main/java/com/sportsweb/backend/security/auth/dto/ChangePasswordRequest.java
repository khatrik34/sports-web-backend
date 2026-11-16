package com.sportsweb.backend.security.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Self-service password change: the current password is required as proof of possession. */
public record ChangePasswordRequest(
        @NotBlank @Size(max = 200) String currentPassword,
        @NotBlank @Size(max = 200) String newPassword) {
}
