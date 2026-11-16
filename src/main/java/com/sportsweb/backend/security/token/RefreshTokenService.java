package com.sportsweb.backend.security.token;

import com.sportsweb.backend.config.JwtProperties;
import com.sportsweb.backend.security.user.UserAccountRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Issues, rotates and revokes refresh tokens.
 *
 * <p>Revocation is only performed by the server: clients never send a "revoke" flag they could
 * forge, and every rotation invalidates the presented token.</p>
 */
@Service
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository repository;
    private final UserAccountRepository userAccountRepository;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(RefreshTokenRepository repository,
                              UserAccountRepository userAccountRepository,
                              JwtProperties jwtProperties) {
        this.repository = repository;
        this.userAccountRepository = userAccountRepository;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public IssuedRefreshToken issue(UUID userId, String ipAddress, String userAgent) {
        CreatedToken created = create(userId, ipAddress, userAgent);
        return new IssuedRefreshToken(created.rawValue(), created.expiresAt());
    }

    /** Revokes the presented token and issues a replacement pointing back at its predecessor. */
    @Transactional
    public IssuedRefreshToken rotate(RefreshTokenSnapshot current, String ipAddress, String userAgent) {
        CreatedToken created = create(current.userId(), ipAddress, userAgent);
        repository.findById(current.tokenId()).ifPresent(previous -> {
            if (previous.getRevokedAt() == null) {
                previous.setRevokedAt(Instant.now());
                previous.setRevokedReason("ROTATED");
                previous.setReplacedByTokenId(created.entityId());
            }
        });
        return new IssuedRefreshToken(created.rawValue(), created.expiresAt());
    }

    @Transactional(readOnly = true)
    public Optional<RefreshTokenSnapshot> find(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return repository.findByTokenHash(hash(rawToken.trim()))
                .map(token -> new RefreshTokenSnapshot(
                        token.getId(),
                        token.getUser().getId(),
                        token.getExpiresAt(),
                        token.getRevokedAt()));
    }

    @Transactional
    public void revoke(UUID tokenId, String reason) {
        repository.findById(tokenId).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(Instant.now());
                token.setRevokedReason(truncate(reason));
            }
        });
    }

    /**
     * Revokes every active session of a user. Runs in its own transaction so that a caller
     * which then throws (disabled account, token reuse) still leaves the revocation committed.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int revokeAllForUser(UUID userId, String reason) {
        return repository.revokeAllForUser(userId, Instant.now(), truncate(reason));
    }

    @Scheduled(cron = "${app.security.jwt.refresh-token-cleanup-cron:0 30 3 * * *}")
    @Transactional
    public void purgeExpiredTokens() {
        repository.deleteExpiredBefore(Instant.now().minus(jwtProperties.getRefreshTokenTtl()));
    }

    private CreatedToken create(UUID userId, String ipAddress, String userAgent) {
        var userReference = userAccountRepository.getReferenceById(userId);
        String rawValue = generateRawToken();
        RefreshToken token = new RefreshToken();
        token.setUser(userReference);
        token.setTokenHash(hash(rawValue));
        token.setIssuedAt(Instant.now());
        token.setExpiresAt(Instant.now().plus(jwtProperties.getRefreshTokenTtl()));
        token.setIpAddress(truncateIp(ipAddress));
        token.setUserAgent(truncateUserAgent(userAgent));
        repository.save(token);
        return new CreatedToken(token.getId(), rawValue, token.getExpiresAt());
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String truncate(String value) {
        return value == null || value.length() <= 32 ? value : value.substring(0, 32);
    }

    private String truncateIp(String value) {
        return value == null || value.length() <= 64 ? value : value.substring(0, 64);
    }

    private String truncateUserAgent(String value) {
        return value == null || value.length() <= 512 ? value : value.substring(0, 512);
    }

    private record CreatedToken(UUID entityId, String rawValue, Instant expiresAt) {
    }

    public record IssuedRefreshToken(String value, Instant expiresAt) {
    }

    public record RefreshTokenSnapshot(UUID tokenId, UUID userId, Instant expiresAt, Instant revokedAt) {

        public boolean isRevoked() {
            return revokedAt != null;
        }

        public boolean isExpired(Instant now) {
            return !expiresAt.isAfter(now);
        }
    }
}
