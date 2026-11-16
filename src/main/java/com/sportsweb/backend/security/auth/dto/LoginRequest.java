package com.sportsweb.backend.security.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "is required")
        @Email(message = "must be a valid email address")
        @Size(max = 320, message = "must be at most 320 characters")
        String email,

        @NotBlank(message = "is required")
        @Size(max = 128, message = "must be at most 128 characters")
        String password) {
}
