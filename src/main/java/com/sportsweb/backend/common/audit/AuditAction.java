package com.sportsweb.backend.common.audit;

/** Every auditable event. Stored as text, so new constants are backwards compatible. */
public enum AuditAction {
    AUTH_LOGIN_SUCCEEDED,
    AUTH_LOGIN_FAILED,
    AUTH_TOKEN_REFRESHED,
    AUTH_LOGOUT,
    AUTH_REFRESH_TOKEN_REUSE,
    BOOTSTRAP_ADMIN_CREATED,
    USER_CREATED,
    USER_UPDATED,
    USER_ROLES_CHANGED,
    USER_PASSWORD_RESET,
    USER_UNLOCKED,
    NEWS_CREATED,
    NEWS_UPDATED,
    NEWS_PUBLISHED,
    NEWS_UNPUBLISHED,
    NEWS_ARCHIVED,
    NEWS_CATEGORY_CREATED,
    NEWS_CATEGORY_UPDATED
}
