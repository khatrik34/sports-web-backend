package com.sportsweb.backend.security.user;

/** Permission codes seeded by migration V2 and enforced in {@code @PreAuthorize}. */
public final class PermissionCodes {

    public static final String NEWS_READ = "news:read";
    public static final String NEWS_WRITE = "news:write";
    public static final String NEWS_PUBLISH = "news:publish";
    public static final String USERS_READ = "users:read";
    public static final String USERS_MANAGE = "users:manage";
    public static final String ROLES_READ = "roles:read";
    public static final String AUDIT_READ = "audit:read";
    public static final String CRICKET_READ = "cricket:read";
    public static final String CRICKET_WRITE = "cricket:write";

    private PermissionCodes() {
    }
}
