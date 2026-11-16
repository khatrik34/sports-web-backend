package com.sportsweb.backend.security.auth.dto;

import com.sportsweb.backend.security.user.Authorities;
import com.sportsweb.backend.security.user.Role;
import com.sportsweb.backend.security.user.UserAccount;

import java.util.List;
import java.util.UUID;

/** Safe representation of the signed-in staff member: never includes credentials. */
public record AuthenticatedUserResponse(
        UUID id,
        String email,
        String displayName,
        boolean mustChangePassword,
        List<String> roles,
        List<String> permissions) {

    public static AuthenticatedUserResponse from(UserAccount user) {
        List<String> roles = user.getRoles().stream().map(Role::getCode).sorted().toList();
        List<String> permissions = Authorities.of(user).stream()
                .filter(authority -> !authority.startsWith("ROLE_"))
                .toList();
        return new AuthenticatedUserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.isMustChangePassword(),
                roles,
                permissions);
    }
}
