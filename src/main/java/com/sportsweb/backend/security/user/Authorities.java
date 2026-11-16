package com.sportsweb.backend.security.user;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds the authority strings carried by the access token:
 * {@code ROLE_<CODE>} for coarse role checks and the permission codes themselves.
 */
public final class Authorities {

    private static final String ROLE_PREFIX = "ROLE_";

    private Authorities() {
    }

    public static List<String> of(UserAccount user) {
        Set<String> authorities = new LinkedHashSet<>();
        for (Role role : user.getRoles()) {
            authorities.add(ROLE_PREFIX + role.getCode());
            authorities.addAll(role.permissionCodes());
        }
        return List.copyOf(authorities);
    }
}
