package com.sportsweb.backend.news;

import com.sportsweb.backend.common.audit.AuditAction;
import com.sportsweb.backend.common.audit.AuditService;
import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.common.util.Slugs;
import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.common.web.Pageables;
import com.sportsweb.backend.news.dto.AdminNewsDetail;
import com.sportsweb.backend.news.dto.AdminNewsSummary;
import com.sportsweb.backend.news.dto.NewsUpsertRequest;
import com.sportsweb.backend.news.dto.PublishRequest;
import com.sportsweb.backend.security.CurrentUserProvider;
import com.sportsweb.backend.security.user.UserAccount;
import com.sportsweb.backend.security.user.UserAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Editorial workflow: draft, publish (now or scheduled), unpublish, archive.
 *
 * <p>Only this service may change editorial state, and every transition is audited. There is
 * no delete in this phase: articles are archived so URLs are never silently reused.</p>
 */
@Service
public class NewsAdminService {

    private final NewsArticleRepository articleRepository;
    private final NewsCategoryRepository categoryRepository;
    private final UserAccountRepository userRepository;
    private final NewsMapper mapper;
    private final NewsUrlBuilder urlBuilder;
    private final AuditService auditService;
    private final CurrentUserProvider currentUserProvider;

    public NewsAdminService(NewsArticleRepository articleRepository,
                            NewsCategoryRepository categoryRepository,
                            UserAccountRepository userRepository,
                            NewsMapper mapper,
                            NewsUrlBuilder urlBuilder,
                            AuditService auditService,
                            CurrentUserProvider currentUserProvider) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.urlBuilder = urlBuilder;
        this.auditService = auditService;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminNewsSummary> list(String status, String categorySlug, String query, int page, int size) {
        Specification<NewsArticle> specification = (root, criteriaQuery, cb) -> cb.conjunction();
        if (status != null && !status.isBlank()) {
            specification = specification.and(NewsSpecifications.withStatus(parseStatus(status)));
        }
        if (categorySlug != null && !categorySlug.isBlank()) {
            specification = specification.and(NewsSpecifications.inCategory(categorySlug.trim()));
        }
        if (query != null && !query.isBlank()) {
            specification = specification.and(NewsSpecifications.matchingText(query));
        }
        Page<NewsArticle> result = articleRepository.findAll(specification,
                Pageables.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt")));
        return PageResponse.from(result, mapper::toAdminSummary);
    }

    @Transactional(readOnly = true)
    public AdminNewsDetail get(UUID id) {
        return mapper.toAdminDetail(loadArticle(id));
    }

    @Transactional
    public AdminNewsDetail create(NewsUpsertRequest request) {
        NewsArticle article = new NewsArticle();
        article.setStatus(NewsStatus.DRAFT);
        article.setSlug(resolveSlugForCreate(request.slug(), request.title()));
        applyEditableFields(article, request);

        UserAccount author = userRepository.findById(currentUserProvider.requireUserId()).orElse(null);
        article.setAuthor(author);
        if (article.getAuthorDisplayName() == null && author != null) {
            article.setAuthorDisplayName(author.getDisplayName());
        }

        articleRepository.save(article);
        auditService.record(AuditAction.NEWS_CREATED, "news_article", article.getId().toString(),
                "Created draft '" + article.getTitle() + "'", null, snapshot(article));
        return mapper.toAdminDetail(article);
    }

    @Transactional
    public AdminNewsDetail update(UUID id, NewsUpsertRequest request) {
        NewsArticle article = loadArticle(id);
        Map<String, Object> before = snapshot(article);

        if (request.slug() != null && !request.slug().isBlank()) {
            String desired = normalizeSlug(request.slug());
            if (!desired.equals(article.getSlug())) {
                if (articleRepository.existsBySlugAndIdNot(desired, id)) {
                    throw ApiException.conflict("slug_taken", "Another article already uses this slug.");
                }
                article.setSlug(desired);
            }
        }
        applyEditableFields(article, request);
        articleRepository.save(article);

        auditService.record(AuditAction.NEWS_UPDATED, "news_article", article.getId().toString(),
                "Updated article '" + article.getTitle() + "'", before, snapshot(article));
        return mapper.toAdminDetail(article);
    }

    @Transactional
    public AdminNewsDetail publish(UUID id, PublishRequest request) {
        NewsArticle article = loadArticle(id);
        if (article.getStatus() == NewsStatus.ARCHIVED) {
            throw ApiException.conflict("invalid_transition",
                    "An archived article cannot be published in this phase.");
        }
        if (isBlank(article.getTitle()) || isBlank(article.getBody())) {
            throw ApiException.badRequest("not_publishable", "Title and body are required before publishing.");
        }
        if (isBlank(article.getSummary())) {
            throw ApiException.badRequest("not_publishable", "A summary is required before publishing.");
        }

        Map<String, Object> before = snapshot(article);
        Instant publishedAt = request != null && request.publishedAt() != null
                ? request.publishedAt()
                : Instant.now();

        article.setStatus(NewsStatus.PUBLISHED);
        article.setPublishedAt(publishedAt);
        article.setArchivedAt(null);
        if (isBlank(article.getCanonicalPath())) {
            article.setCanonicalPath(urlBuilder.derivePath(article));
        }
        articleRepository.save(article);

        String detail = publishedAt.isAfter(Instant.now())
                ? "Scheduled article '" + article.getTitle() + "' for " + publishedAt
                : "Published article '" + article.getTitle() + "'";
        auditService.record(AuditAction.NEWS_PUBLISHED, "news_article", article.getId().toString(),
                detail, before, snapshot(article));
        return mapper.toAdminDetail(article);
    }

    @Transactional
    public AdminNewsDetail unpublish(UUID id) {
        NewsArticle article = loadArticle(id);
        if (article.getStatus() != NewsStatus.PUBLISHED) {
            throw ApiException.conflict("invalid_transition", "Only a published article can be unpublished.");
        }
        Map<String, Object> before = snapshot(article);
        article.setStatus(NewsStatus.DRAFT);
        article.setPublishedAt(null);
        article.setArchivedAt(null);
        articleRepository.save(article);

        auditService.record(AuditAction.NEWS_UNPUBLISHED, "news_article", article.getId().toString(),
                "Unpublished article '" + article.getTitle() + "'", before, snapshot(article));
        return mapper.toAdminDetail(article);
    }

    @Transactional
    public AdminNewsDetail archive(UUID id) {
        NewsArticle article = loadArticle(id);
        if (article.getStatus() == NewsStatus.ARCHIVED) {
            return mapper.toAdminDetail(article);
        }
        Map<String, Object> before = snapshot(article);
        article.setStatus(NewsStatus.ARCHIVED);
        article.setArchivedAt(Instant.now());
        articleRepository.save(article);

        auditService.record(AuditAction.NEWS_ARCHIVED, "news_article", article.getId().toString(),
                "Archived article '" + article.getTitle() + "'", before, snapshot(article));
        return mapper.toAdminDetail(article);
    }

    private void applyEditableFields(NewsArticle article, NewsUpsertRequest request) {
        article.setTitle(request.title().trim());
        article.setSubtitle(blankToNull(request.subtitle()));
        article.setSummary(blankToNull(request.summary()));
        article.setBody(request.body());
        article.setBodyFormat(request.bodyFormat() == null ? BodyFormat.HTML : request.bodyFormat());
        article.setCategory(request.categorySlug() == null || request.categorySlug().isBlank()
                ? null
                : resolveCategory(request.categorySlug()));
        article.setAuthorDisplayName(blankToNull(request.authorDisplayName()));
        article.setCoverImageUrl(blankToNull(request.coverImageUrl()));
        article.setCoverImageAlt(blankToNull(request.coverImageAlt()));
        article.setMetaTitle(blankToNull(request.metaTitle()));
        article.setMetaDescription(blankToNull(request.metaDescription()));
        article.setOgTitle(blankToNull(request.ogTitle()));
        article.setOgDescription(blankToNull(request.ogDescription()));
        article.setOgImageUrl(blankToNull(request.ogImageUrl()));
        article.setKeywords(blankToNull(request.keywords()));
        article.setFeatured(Boolean.TRUE.equals(request.featured()));

        String robots = blankToNull(request.robots());
        article.setRobots(robots == null ? "index,follow" : robots);

        if (request.canonicalPath() != null) {
            String canonical = blankToNull(request.canonicalPath());
            if (canonical != null && !canonical.startsWith("/")) {
                throw ApiException.badRequest("invalid_canonical_path", "canonicalPath must start with '/'.");
            }
            article.setCanonicalPath(canonical);
        }
    }

    private NewsCategory resolveCategory(String slug) {
        String normalized = slug.trim().toLowerCase(Locale.ROOT);
        return categoryRepository.findBySlug(normalized)
                .orElseThrow(() -> ApiException.badRequest("unknown_category", "Unknown category: " + slug));
    }

    private String resolveSlugForCreate(String requestedSlug, String title) {
        if (requestedSlug != null && !requestedSlug.isBlank()) {
            String normalized = normalizeSlug(requestedSlug);
            if (articleRepository.existsBySlug(normalized)) {
                throw ApiException.conflict("slug_taken", "Another article already uses this slug.");
            }
            return normalized;
        }
        return generateUniqueSlug(title);
    }

    private String generateUniqueSlug(String title) {
        String base = Slugs.slugify(title);
        if (base.isBlank()) {
            base = "article";
        }
        String candidate = base;
        int attempt = 1;
        while (articleRepository.existsBySlug(candidate)) {
            attempt++;
            if (attempt > 100) {
                throw ApiException.conflict("slug_unavailable", "Could not derive a unique slug; please provide one.");
            }
            candidate = Slugs.withSuffix(base, attempt);
        }
        return candidate;
    }

    private String normalizeSlug(String rawSlug) {
        String candidate = Slugs.slugify(rawSlug);
        if (candidate.isBlank() || !Slugs.isValid(candidate)) {
            throw ApiException.badRequest("invalid_slug", "A slug may only contain lower case letters, digits and hyphens.");
        }
        return candidate;
    }

    private NewsStatus parseStatus(String status) {
        try {
            return NewsStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("invalid_status", "Unknown status: " + status);
        }
    }

    private NewsArticle loadArticle(UUID id) {
        return articleRepository.findById(id).orElseThrow(() -> ApiException.notFound("Article"));
    }

    /** Compact audit snapshot: status and metadata, never the full article body. */
    private Map<String, Object> snapshot(NewsArticle article) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("slug", article.getSlug());
        snapshot.put("title", article.getTitle());
        snapshot.put("status", article.getStatus());
        snapshot.put("categorySlug", article.getCategory() == null ? null : article.getCategory().getSlug());
        snapshot.put("featured", article.isFeatured());
        snapshot.put("publishedAt", article.getPublishedAt());
        snapshot.put("canonicalPath", article.getCanonicalPath());
        return snapshot;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
