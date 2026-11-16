package com.sportsweb.backend.seo;

import com.sportsweb.backend.common.audit.AuditAction;
import com.sportsweb.backend.common.audit.AuditService;
import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.common.util.Texts;
import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.common.web.Pageables;
import com.sportsweb.backend.seo.dto.PublicRedirect;
import com.sportsweb.backend.seo.dto.RedirectRequest;
import com.sportsweb.backend.seo.dto.RedirectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Redirect management plus the anonymous resolver the website calls before rendering a 404. */
@Service
public class RedirectService {

    private static final Set<Integer> ALLOWED_STATUS = Set.of(301, 302, 307, 308, 410);

    private final RedirectRepository repository;
    private final AuditService auditService;

    public RedirectService(RedirectRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<RedirectResponse> list(String query, Boolean enabled, int page, int size) {
        Specification<Redirect> specification = (root, criteriaQuery, cb) -> cb.conjunction();
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, criteriaQuery, cb) -> cb.or(
                    cb.like(cb.lower(root.get("fromPath")), pattern),
                    cb.like(cb.lower(root.get("toPath")), pattern)));
        }
        if (enabled != null) {
            specification = specification.and((root, criteriaQuery, cb) -> cb.equal(root.get("enabled"), enabled));
        }
        Page<Redirect> result = repository.findAll(specification, Pageables.of(page, size, Sort.by("fromPath")));
        return PageResponse.from(result, this::toResponse);
    }

    @Transactional(readOnly = true)
    public RedirectResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public RedirectResponse create(RedirectRequest request) {
        String fromPath = normalizePath(request.fromPath());
        if (repository.existsByFromPath(fromPath)) {
            throw ApiException.conflict("redirect_exists", "A redirect for this path already exists.");
        }
        Redirect redirect = new Redirect();
        redirect.setFromPath(fromPath);
        apply(redirect, request);
        repository.save(redirect);
        auditService.record(AuditAction.REDIRECT_CREATED, "seo_redirect", redirect.getId().toString(),
                "Created redirect " + fromPath, null, snapshot(redirect));
        return toResponse(redirect);
    }

    @Transactional
    public RedirectResponse update(UUID id, RedirectRequest request) {
        Redirect redirect = load(id);
        Map<String, Object> before = snapshot(redirect);
        String fromPath = normalizePath(request.fromPath());
        if (!fromPath.equals(redirect.getFromPath()) && repository.existsByFromPathAndIdNot(fromPath, id)) {
            throw ApiException.conflict("redirect_exists", "A redirect for this path already exists.");
        }
        redirect.setFromPath(fromPath);
        apply(redirect, request);
        repository.save(redirect);
        auditService.record(AuditAction.REDIRECT_UPDATED, "seo_redirect", id.toString(),
                "Updated redirect " + fromPath, before, snapshot(redirect));
        return toResponse(redirect);
    }

    @Transactional
    public void delete(UUID id) {
        Redirect redirect = load(id);
        Map<String, Object> before = snapshot(redirect);
        repository.delete(redirect);
        auditService.record(AuditAction.REDIRECT_DELETED, "seo_redirect", id.toString(),
                "Deleted redirect " + redirect.getFromPath(), before, null);
    }

    @Transactional(readOnly = true)
    public PublicRedirect resolve(String path) {
        String normalized = normalizePath(path);
        Redirect redirect = repository.findByFromPath(normalized)
                .filter(Redirect::isEnabled)
                .orElseThrow(() -> ApiException.notFound("Redirect"));
        return new PublicRedirect(redirect.getFromPath(), redirect.getToPath(), redirect.getStatusCode());
    }

    private void apply(Redirect redirect, RedirectRequest request) {
        int statusCode = request.statusCode() == null ? 301 : request.statusCode();
        if (!ALLOWED_STATUS.contains(statusCode)) {
            throw ApiException.badRequest("invalid_status",
                    "Status code must be one of 301, 302, 307, 308 or 410.");
        }
        String toPath = Texts.blankToNull(request.toPath());
        if (statusCode == 410) {
            redirect.setToPath(null);
        } else {
            if (toPath == null) {
                throw ApiException.badRequest("target_required",
                        "A redirect needs a target path; use 410 to retire a URL.");
            }
            redirect.setToPath(normalizePath(toPath));
        }
        redirect.setStatusCode(statusCode);
        redirect.setNote(Texts.blankToNull(request.note()));
        redirect.setEnabled(request.enabled() == null || request.enabled());
    }

    private String normalizePath(String path) {
        String value = Texts.blankToNull(path);
        if (value == null) {
            throw ApiException.badRequest("invalid_path", "A path is required.");
        }
        if (value.contains("?") || value.contains("#")) {
            throw ApiException.badRequest("invalid_path", "Store only the path, without query string or fragment.");
        }
        String normalized = value.startsWith("/") ? value : "/" + value;
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private Redirect load(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Redirect"));
    }

    private RedirectResponse toResponse(Redirect redirect) {
        return new RedirectResponse(redirect.getId(), redirect.getFromPath(), redirect.getToPath(),
                redirect.getStatusCode(), redirect.getNote(), redirect.isEnabled(),
                redirect.getCreatedAt(), redirect.getUpdatedAt(), redirect.getVersion());
    }

    private Map<String, Object> snapshot(Redirect redirect) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("fromPath", redirect.getFromPath());
        values.put("toPath", redirect.getToPath());
        values.put("statusCode", redirect.getStatusCode());
        values.put("enabled", redirect.isEnabled());
        return values;
    }
}
