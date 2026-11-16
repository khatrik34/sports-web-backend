package com.sportsweb.backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Base64;

/**
 * JWT configuration. The signing key is supplied by environment (base64, 32+ bytes) and is
 * validated at startup: the application refuses to boot with a missing or weak secret.
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    private static final int MIN_KEY_BYTES = 32;

    private String secret = "";
    private String issuer = "sports-web-backend";
    private Duration accessTokenTtl = Duration.ofMinutes(15);
    private Duration refreshTokenTtl = Duration.ofDays(14);

    @PostConstruct
    void validate() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "APP_JWT_SECRET is not set. Generate one with 'openssl rand -base64 48' and provide it as base64.");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secret.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("APP_JWT_SECRET must be base64 encoded (e.g. output of 'openssl rand -base64 48').", ex);
        }
        if (decoded.length < MIN_KEY_BYTES) {
            throw new IllegalStateException("APP_JWT_SECRET must decode to at least " + MIN_KEY_BYTES + " bytes for HS256.");
        }
        if (accessTokenTtl == null || accessTokenTtl.isZero() || accessTokenTtl.isNegative()) {
            throw new IllegalStateException("app.security.jwt.access-token-ttl must be a positive duration.");
        }
        if (refreshTokenTtl == null || refreshTokenTtl.compareTo(accessTokenTtl) <= 0) {
            throw new IllegalStateException("app.security.jwt.refresh-token-ttl must be longer than the access token ttl.");
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(Duration refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }
}
