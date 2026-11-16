package com.sportsweb.backend.security;

import com.sportsweb.backend.common.error.ApiException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Reads the authenticated principal from the validated JWT in the security context. */
@Component
public class CurrentUserProvider {

    public Optional<CurrentUser> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            UUID userId;
            try {
                userId = UUID.fromString(jwt.getSubject());
            } catch (IllegalArgumentException | NullPointerException ex) {
                return Optional.empty();
            }
            return Optional.of(new CurrentUser(userId, jwt.getClaimAsString("email")));
        }
        return Optional.empty();
    }

    public CurrentUser requireCurrentUser() {
        return currentUser().orElseThrow(() -> ApiException.unauthorized("unauthenticated", "Authentication is required."));
    }

    public UUID requireUserId() {
        return requireCurrentUser().userId();
    }

    public record CurrentUser(UUID userId, String email) {
    }
}
