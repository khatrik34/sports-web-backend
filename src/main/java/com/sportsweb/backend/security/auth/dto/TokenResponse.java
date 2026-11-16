package com.sportsweb.backend.security.auth.dto;

import java.time.Instant;

public record TokenResponse(
        String accessToken,
        String tokenType,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        AuthenticatedUserResponse user) {
}
