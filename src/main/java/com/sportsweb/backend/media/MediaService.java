package com.sportsweb.backend.media;

import com.sportsweb.backend.common.audit.AuditAction;
import com.sportsweb.backend.common.audit.AuditService;
import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.common.util.Texts;
import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.common.web.Pageables;
import com.sportsweb.backend.media.dto.MediaRequest;
import com.sportsweb.backend.media.dto.MediaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Metadata for externally hosted media. No binary upload happens here (documented limitation). */
@Service
public class MediaService {

    private final MediaAssetRepository repository;
    private final AuditService auditService;

    public MediaService(MediaAssetRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<MediaResponse> list(String query, int page, int size) {
        Specification<MediaAsset> specification = (root, criteriaQuery, cb) -> cb.conjunction();
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, criteriaQuery, cb) -> cb.or(
                    cb.like(cb.lower(root.get("url")), pattern),
                    cb.like(cb.lower(root.get("filename")), pattern)));
        }
        Page<MediaAsset> result = repository.findAll(specification,
                Pageables.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResponse.from(result, this::toResponse);
    }

    @Transactional(readOnly = true)
    public MediaResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public MediaResponse create(MediaRequest request) {
        MediaAsset asset = new MediaAsset();
        apply(asset, request);
        repository.save(asset);
        auditService.record(AuditAction.MEDIA_CREATED, "media_asset", asset.getId().toString(),
                "Registered media " + asset.getUrl(), null, snapshot(asset));
        return toResponse(asset);
    }

    @Transactional
    public MediaResponse update(UUID id, MediaRequest request) {
        MediaAsset asset = load(id);
        Map<String, Object> before = snapshot(asset);
        apply(asset, request);
        repository.save(asset);
        auditService.record(AuditAction.MEDIA_CREATED, "media_asset", id.toString(),
                "Updated media " + asset.getUrl(), before, snapshot(asset));
        return toResponse(asset);
    }

    @Transactional
    public void delete(UUID id) {
        MediaAsset asset = load(id);
        Map<String, Object> before = snapshot(asset);
        repository.delete(asset);
        auditService.record(AuditAction.MEDIA_DELETED, "media_asset", id.toString(),
                "Deleted media " + asset.getUrl(), before, null);
    }

    private void apply(MediaAsset asset, MediaRequest request) {
        String url = request.url().trim();
        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("/")) {
            throw ApiException.badRequest("invalid_url", "Use an absolute https URL or a site-relative path.");
        }
        asset.setUrl(url);
        asset.setFilename(Texts.blankToNull(request.filename()));
        asset.setContentType(Texts.blankToNull(request.contentType()));
        asset.setSizeBytes(request.sizeBytes());
        asset.setWidth(request.width());
        asset.setHeight(request.height());
        asset.setAltText(Texts.blankToNull(request.altText()));
        asset.setCaption(Texts.blankToNull(request.caption()));
        asset.setCredit(Texts.blankToNull(request.credit()));
    }

    private MediaAsset load(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Media asset"));
    }

    private MediaResponse toResponse(MediaAsset asset) {
        return new MediaResponse(asset.getId(), asset.getUrl(), asset.getFilename(), asset.getContentType(),
                asset.getSizeBytes(), asset.getWidth(), asset.getHeight(), asset.getAltText(),
                asset.getCaption(), asset.getCredit(), asset.getCreatedAt(), asset.getUpdatedAt(),
                asset.getVersion());
    }

    private Map<String, Object> snapshot(MediaAsset asset) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("url", asset.getUrl());
        values.put("altText", asset.getAltText());
        return values;
    }
}
