package com.sportsweb.backend.security;

import com.sportsweb.backend.config.JwtProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Issues short lived, stateless access tokens. Refresh tokens are opaque and stored hashed. */
@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public JwtTokenService(JwtEncoder jwtEncoder, JwtProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public IssuedAccessToken issue(UUID userId, String email, Collection<String> authorities,
                                   boolean mustChangePassword) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.getAccessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(userId.toString())
                .claim("email", email)
                .claim("authorities", List.copyOf(authorities))
                // Lets the API refuse business calls until the forced password change is done.
                .claim("must_change_password", mustChangePassword)
                .id(UUID.randomUUID().toString())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(header, claims));
        return new IssuedAccessToken(jwt.getTokenValue(), expiresAt);
    }

    public record IssuedAccessToken(String value, Instant expiresAt) {
    }
}
