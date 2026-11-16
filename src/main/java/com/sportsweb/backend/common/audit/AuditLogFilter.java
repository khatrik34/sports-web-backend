package com.sportsweb.backend.common.audit;

import java.time.Instant;

public record AuditLogFilter(
        AuditAction action,
        String entityType,
        String entityId,
        Instant from,
        Instant to) {
}
