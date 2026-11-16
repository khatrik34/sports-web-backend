package com.sportsweb.backend.common.audit;

/** Client metadata captured for the audit trail. */
public record RequestMetadata(String ipAddress, String userAgent) {

    public static final RequestMetadata EMPTY = new RequestMetadata(null, null);
}
