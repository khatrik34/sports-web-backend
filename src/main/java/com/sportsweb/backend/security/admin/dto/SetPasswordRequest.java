package com.sportsweb.backend.security.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetPasswordRequest(
        @NotBlank(message = "is required")
        @Size(max = 128, message = "must be at most 128 characters")
        String password,

        Boolean mustChangePassword) {
}
