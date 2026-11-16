package com.sportsweb.backend.common.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Writes audit records.
 *
 * <p>Two entry points on purpose:</p>
 * <ul>
 *   <li>{@link #record} joins the caller's transaction, so a business change and its audit
 *       row are committed together (or not at all).</li>
 *   <li>{@link #recordSecurityEvent} runs in its own transaction, so failed logins and
 *       refresh-token reuse are still recorded when the caller rolls back.</li>
 * </ul>
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper;
    private final RequestMetadataProvider metadataProvider;

    public AuditService(AuditLogRepository repository,
                        ObjectMapper objectMapper,
                        RequestMetadataProvider metadataProvider) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.metadataProvider = metadataProvider;
    }

    @Transactional
    public void record(AuditAction action,
                       String entityType,
                       String entityId,
                       String summary,
                       Map<String, Object> before,
                       Map<String, Object> after) {
        Actor actor = currentActor();
        persist(action, AuditOutcome.SUCCESS, entityType, entityId, summary,
                actor.userId(), actor.email(), before, after);
    }

    /** Security events must survive the exception that the caller is about to throw. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSecurityEvent(AuditAction action,
                                    AuditOutcome outcome,
                                    String actorEmail,
                                    UUID actorUserId,
                                    String summary) {
        persist(action, outcome, "auth", actorEmail, summary, actorUserId, actorEmail, null, null);
    }

    private void persist(AuditAction action,
                         AuditOutcome outcome,
                         String entityType,
                         String entityId,
                         String summary,
                         UUID actorUserId,
                         String actorEmail,
                         Map<String, Object> before,
                         Map<String, Object> after) {
        RequestMetadata metadata = metadataProvider.current();
        AuditLog entry = new AuditLog();
        entry.setAction(action);
        entry.setOutcome(outcome);
        entry.setEntityType(entityType);
        entry.setEntityId(truncate(entityId, 128));
        entry.setSummary(truncate(summary, 512));
        entry.setActorUserId(actorUserId);
        entry.setActorEmail(truncate(actorEmail, 320));
        entry.setIpAddress(metadata.ipAddress());
        entry.setUserAgent(metadata.userAgent());
        entry.setBeforeState(toJson(before));
        entry.setAfterState(toJson(after));
        repository.save(entry);
    }

    private Actor currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt) {
            UUID userId = parseUuid(jwt.getSubject());
            return new Actor(userId, jwt.getClaimAsString("email"));
        }
        return new Actor(null, null);
    }

    private UUID parseUuid(String value) {
        try {
            return value == null ? null : UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String toJson(Map<String, Object> snapshot) {
        if (snapshot == null || snapshot.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            log.warn("Could not serialise audit snapshot", ex);
            return null;
        }
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private record Actor(UUID userId, String email) {
    }
}
