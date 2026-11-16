package com.sportsweb.backend.common.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.common.web.Pageables;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditQueryService {

    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper;

    public AuditQueryService(AuditLogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(AuditLogFilter filter, int page, int size) {
        Specification<AuditLog> specification = (root, query, cb) -> cb.conjunction();

        if (filter.action() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("action"), filter.action()));
        }
        if (hasText(filter.entityType())) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("entityType"), filter.entityType()));
        }
        if (hasText(filter.entityId())) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("entityId"), filter.entityId()));
        }
        if (filter.from() != null) {
            specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), filter.from()));
        }
        if (filter.to() != null) {
            specification = specification.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"), filter.to()));
        }

        Page<AuditLog> result = repository.findAll(specification,
                Pageables.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt")));
        return PageResponse.from(result, this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog entry) {
        return new AuditLogResponse(
                entry.getId(),
                entry.getOccurredAt(),
                entry.getAction(),
                entry.getOutcome(),
                entry.getEntityType(),
                entry.getEntityId(),
                entry.getSummary(),
                entry.getActorUserId(),
                entry.getActorEmail(),
                entry.getIpAddress(),
                entry.getUserAgent(),
                readTree(entry.getBeforeState()),
                readTree(entry.getAfterState()));
    }

    private JsonNode readTree(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
