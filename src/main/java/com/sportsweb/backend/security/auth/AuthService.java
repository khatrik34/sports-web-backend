package com.sportsweb.backend.security.auth;

import com.sportsweb.backend.common.audit.AuditAction;
import com.sportsweb.backend.common.audit.AuditOutcome;
import com.sportsweb.backend.common.audit.AuditService;
import com.sportsweb.backend.common.audit.RequestMetadata;
import com.sportsweb.backend.common.audit.RequestMetadataProvider;
import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.security.CurrentUserProvider;
import com.sportsweb.backend.security.JwtTokenService;
import com.sportsweb.backend.security.auth.dto.AuthenticatedUserResponse;
import com.sportsweb.backend.security.auth.dto.LoginRequest;
import com.sportsweb.backend.security.auth.dto.LogoutRequest;
import com.sportsweb.backend.security.auth.dto.RefreshRequest;
import com.sportsweb.backend.security.auth.dto.TokenResponse;
import com.sportsweb.backend.security.token.RefreshTokenService;
import com.sportsweb.backend.security.token.RefreshTokenService.IssuedRefreshToken;
import com.sportsweb.backend.security.token.RefreshTokenService.RefreshTokenSnapshot;
import com.sportsweb.backend.security.user.Authorities;
import com.sportsweb.backend.security.user.UserAccount;
import com.sportsweb.backend.security.user.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Credential handling. Access tokens are stateless; refresh tokens are opaque, rotated on
 * every use and revoked on logout, password reset, disable or suspected reuse.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserAccountRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final RequestMetadataProvider metadataProvider;
    private final CurrentUserProvider currentUserProvider;

    public AuthService(UserAccountRepository userRepository,
                       RefreshTokenService refreshTokenService,
                       LoginAttemptService loginAttemptService,
                       JwtTokenService jwtTokenService,
                       PasswordEncoder passwordEncoder,
                       AuditService auditService,
                       RequestMetadataProvider metadataProvider,
                       CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.metadataProvider = metadataProvider;
        this.currentUserProvider = currentUserProvider;
    }

    public TokenResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        Instant now = Instant.now();

        Optional<UserAccount> found = userRepository.findForAuthenticationByEmail(email);
        if (found.isEmpty()) {
            auditService.recordSecurityEvent(AuditAction.AUTH_LOGIN_FAILED, AuditOutcome.FAILURE,
                    email, null, "Sign-in attempt for an unknown account");
            throw invalidCredentials();
        }

        UserAccount user = found.get();

        if (user.isLocked(now)) {
            auditService.recordSecurityEvent(AuditAction.AUTH_LOGIN_FAILED, AuditOutcome.FAILURE,
                    email, user.getId(), "Sign-in attempt on a locked account");
            throw ApiException.forbidden("account_locked",
                    "This account is temporarily locked after repeated failed sign-in attempts.");
        }
        if (!user.isEnabled()) {
            auditService.recordSecurityEvent(AuditAction.AUTH_LOGIN_FAILED, AuditOutcome.FAILURE,
                    email, user.getId(), "Sign-in attempt on a disabled account");
            throw ApiException.forbidden("account_disabled", "This account is disabled.");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttemptService.registerFailure(user.getId());
            throw invalidCredentials();
        }

        loginAttemptService.registerSuccess(user.getId());
        RequestMetadata metadata = metadataProvider.current();
        IssuedRefreshToken refreshToken = refreshTokenService.issue(user.getId(), metadata.ipAddress(), metadata.userAgent());
        return buildResponse(user, refreshToken);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        Instant now = Instant.now();
        RequestMetadata metadata = metadataProvider.current();
        Optional<RefreshTokenSnapshot> found = refreshTokenService.find(request.refreshToken());

        if (found.isEmpty()) {
            auditService.recordSecurityEvent(AuditAction.AUTH_LOGIN_FAILED, AuditOutcome.FAILURE,
                    null, null, "Refresh attempted with an unknown token");
            throw invalidRefreshToken();
        }

        RefreshTokenSnapshot token = found.get();

        if (token.isRevoked()) {
            // A rotated or revoked token came back: treat it as stolen and end every session.
            refreshTokenService.revokeAllForUser(token.userId(), "REUSE_DETECTED");
            auditService.recordSecurityEvent(AuditAction.AUTH_REFRESH_TOKEN_REUSE, AuditOutcome.FAILURE,
                    null, token.userId(), "Revoked refresh token presented again; all sessions revoked");
            log.warn("Refresh token reuse detected for user {}", token.userId());
            throw invalidRefreshToken();
        }
        if (token.isExpired(now)) {
            throw invalidRefreshToken();
        }

        UserAccount user = userRepository.findForAuthenticationById(token.userId())
                .orElseThrow(this::invalidRefreshToken);

        if (!user.isEnabled() || user.isLocked(now)) {
            refreshTokenService.revokeAllForUser(user.getId(), "ACCOUNT_INACTIVE");
            auditService.recordSecurityEvent(AuditAction.AUTH_LOGIN_FAILED, AuditOutcome.FAILURE,
                    user.getEmail(), user.getId(), "Refresh attempted on an inactive account");
            throw ApiException.forbidden("account_disabled", "This account is not active.");
        }

        IssuedRefreshToken rotated = refreshTokenService.rotate(token, metadata.ipAddress(), metadata.userAgent());
        auditService.record(AuditAction.AUTH_TOKEN_REFRESHED, "user", user.getId().toString(),
                "Access token refreshed", null, null);
        return buildResponse(user, rotated);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenService.find(request.refreshToken()).ifPresent(token -> {
            refreshTokenService.revoke(token.tokenId(), "LOGOUT");
            auditService.record(AuditAction.AUTH_LOGOUT, "auth", token.tokenId().toString(),
                    "Refresh token revoked on logout", null, null);
        });
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse me() {
        UUID userId = currentUserProvider.requireUserId();
        UserAccount user = userRepository.findForAuthenticationById(userId)
                .orElseThrow(() -> ApiException.unauthorized("unauthenticated", "The account no longer exists."));
        if (!user.isEnabled()) {
            throw ApiException.forbidden("account_disabled", "This account is disabled.");
        }
        return AuthenticatedUserResponse.from(user);
    }

    private TokenResponse buildResponse(UserAccount user, IssuedRefreshToken refreshToken) {
        JwtTokenService.IssuedAccessToken accessToken =
                jwtTokenService.issue(user.getId(), user.getEmail(), Authorities.of(user));
        return new TokenResponse(
                accessToken.value(),
                "Bearer",
                accessToken.expiresAt(),
                refreshToken.value(),
                refreshToken.expiresAt(),
                AuthenticatedUserResponse.from(user));
    }

    private ApiException invalidCredentials() {
        return ApiException.unauthorized("invalid_credentials", "Email or password is incorrect.");
    }

    private ApiException invalidRefreshToken() {
        return ApiException.unauthorized("invalid_refresh_token", "The refresh token is invalid or has expired.");
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
