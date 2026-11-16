package com.sportsweb.backend.security.admin.dto;

import com.sportsweb.backend.security.user.UserAccount;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Administrator view of a staff account. Never exposes the password hash. */
public record UserResponse(
        UUID id,
        String email,
        String displayName,
        boolean enabled,
        boolean locked,
        Instant lockedUntil,
        Instant lastLoginAt,
        boolean mustChangePassword,
        Instant createdAt,
        Instant updatedAt,
        List<RoleSummary> roles) {

    public static UserResponse from(UserAccount user, Instant now) {
        List<RoleSummary> roles = user.getRoles().stream()
                .map(role -> new RoleSummary(role.getCode(), role.getName()))
                .sorted((left, right) -> left.code().compareTo(right.code()))
                .toList();
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.isEnabled(),
                user.isLocked(now),
                user.getLockedUntil(),
                user.getLastLoginAt(),
                user.isMustChangePassword(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                roles);
    }
}
