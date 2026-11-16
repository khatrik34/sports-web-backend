package com.sportsweb.backend.videos;

import com.sportsweb.backend.common.audit.AuditAction;
import com.sportsweb.backend.common.audit.AuditService;
import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.common.util.SlugResolver;
import com.sportsweb.backend.common.util.Texts;
import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.common.web.Pageables;
import com.sportsweb.backend.videos.dto.AdminVideoResponse;
import com.sportsweb.backend.videos.dto.PublicVideo;
import com.sportsweb.backend.videos.dto.VideoUpsertRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Admin CRUD plus the anonymous read path for videos. No video files are hosted here. */
@Service
public class VideoService {

    private final VideoRepository repository;
    private final VideoUrlBuilder urlBuilder;
    private final AuditService auditService;

    public VideoService(VideoRepository repository, VideoUrlBuilder urlBuilder, AuditService auditService) {
        this.repository = repository;
        this.urlBuilder = urlBuilder;
        this.auditService = auditService;
    }

    // ---------------------------------------------------------------- admin

    @Transactional(readOnly = true)
    public PageResponse<AdminVideoResponse> list(String query, String category, Boolean visible, int page, int size) {
        Specification<Video> specification = (root, criteriaQuery, cb) -> cb.conjunction();
        specification = specification.and(VideoSpecifications.withVisibility(visible));
        if (query != null && !query.isBlank()) {
            specification = specification.and(VideoSpecifications.matchingText(query));
        }
        if (category != null && !category.isBlank()) {
            specification = specification.and(VideoSpecifications.inCategoryLike(category));
        }
        Page<Video> result = repository.findAll(specification,
                Pageables.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResponse.from(result, this::toAdmin);
    }

    @Transactional(readOnly = true)
    public AdminVideoResponse get(UUID id) {
        return toAdmin(load(id));
    }

    @Transactional
    public AdminVideoResponse create(VideoUpsertRequest request) {
        Video video = new Video();
        video.setSlug(SlugResolver.forCreate(request.slug(), request.title(), repository::existsBySlug));
        apply(video, request, true);
        repository.save(video);
        auditService.record(AuditAction.VIDEO_CREATED, "video", video.getId().toString(),
                "Created video '" + video.getTitle() + "'", null, snapshot(video));
        return toAdmin(video);
    }

    @Transactional
    public AdminVideoResponse update(UUID id, VideoUpsertRequest request) {
        Video video = load(id);
        Map<String, Object> before = snapshot(video);
        video.setSlug(SlugResolver.forUpdate(request.slug(), video.getSlug(),
                candidate -> repository.existsBySlugAndIdNot(candidate, id)));
        apply(video, request, false);
        repository.save(video);
        auditService.record(AuditAction.VIDEO_UPDATED, "video", id.toString(),
                "Updated video '" + video.getTitle() + "'", before, snapshot(video));
        return toAdmin(video);
    }

    @Transactional
    public AdminVideoResponse setVisible(UUID id, boolean visible) {
        Video video = load(id);
        if (video.isVisible() == visible) {
            return toAdmin(video);
        }
        Map<String, Object> before = snapshot(video);
        video.setVisible(visible);
        repository.save(video);
        auditService.record(AuditAction.VIDEO_STATUS_CHANGED, "video", id.toString(),
                (visible ? "Shown" : "Hidden") + " video '" + video.getTitle() + "'", before, snapshot(video));
        return toAdmin(video);
    }

    @Transactional
    public void delete(UUID id) {
        Video video = load(id);
        Map<String, Object> before = snapshot(video);
        repository.delete(video);
        auditService.record(AuditAction.VIDEO_DELETED, "video", id.toString(),
                "Deleted video '" + video.getTitle() + "'", before, null);
    }

    // ---------------------------------------------------------------- public

    @Transactional(readOnly = true)
    public PageResponse<PublicVideo> publicList(String category, int page, int size) {
        Instant now = Instant.now();
        Specification<Video> specification = VideoSpecifications.publiclyVisible(now);
        if (category != null && !category.isBlank()) {
            specification = specification.and(VideoSpecifications.inCategoryLike(category));
        }
        Page<Video> result = repository.findAll(specification,
                Pageables.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt")));
        return PageResponse.from(result, this::toPublic);
    }

    @Transactional(readOnly = true)
    public PublicVideo publicGet(String slug) {
        Instant now = Instant.now();
        Video video = repository.findBySlug(slug)
                .filter(candidate -> candidate.isPubliclyVisible(now))
                .orElseThrow(() -> ApiException.notFound("Video"));
        return toPublic(video);
    }

    // ---------------------------------------------------------------- mapping

    private PublicVideo toPublic(Video video) {
        String path = urlBuilder.path(video.getSlug());
        return new PublicVideo(video.getId(), video.getSlug(), video.getTitle(), video.getCategory(),
                video.getThumbnailUrl(), video.getDurationText(), video.getDescription(),
                video.getVideoUrl(), video.getPublishedAt(), path, urlBuilder.absolute(path));
    }

    private AdminVideoResponse toAdmin(Video video) {
        return new AdminVideoResponse(video.getId(), video.getSlug(), video.getTitle(), video.getCategory(),
                video.getDescription(), video.getThumbnailUrl(), video.getVideoUrl(), video.getDurationText(),
                video.getPublishedAt(), video.isVisible(), video.getCreatedAt(), video.getUpdatedAt(),
                video.getVersion());
    }

    private void apply(Video video, VideoUpsertRequest request, boolean creating) {
        video.setTitle(request.title().trim());
        video.setCategory(Texts.blankToNull(request.category()));
        video.setDescription(Texts.blankToNull(request.description()));
        video.setThumbnailUrl(Texts.blankToNull(request.thumbnailUrl()));
        video.setVideoUrl(request.videoUrl().trim());
        video.setDurationText(Texts.blankToNull(request.duration()));
        video.setPublishedAt(request.publishedAt());
        if (request.visible() != null) {
            video.setVisible(request.visible());
        } else if (creating) {
            video.setVisible(true);
        }
    }

    private Video load(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Video"));
    }

    private Map<String, Object> snapshot(Video video) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("slug", video.getSlug());
        values.put("title", video.getTitle());
        values.put("visible", video.isVisible());
        values.put("publishedAt", video.getPublishedAt() == null ? null : video.getPublishedAt().toString());
        return values;
    }
}
