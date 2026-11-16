package com.sportsweb.backend.common.audit;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        Instant occurredAt,
        AuditAction action,
        AuditOutcome outcome,
        String entityType,
        String entityId,
        String summary,
        UUID actorUserId,
        String actorEmail,
        String ipAddress,
        String userAgent,
        JsonNode before,
        JsonNode after) {
}
