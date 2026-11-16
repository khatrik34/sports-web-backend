package com.sportsweb.backend.news;

import com.sportsweb.backend.common.audit.AuditAction;
import com.sportsweb.backend.common.audit.AuditService;
import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.common.util.Slugs;
import com.sportsweb.backend.news.dto.AdminCategoryResponse;
import com.sportsweb.backend.news.dto.CategoryUpsertRequest;
import com.sportsweb.backend.news.dto.PublicCategoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NewsCategoryService {

    private final NewsCategoryRepository categoryRepository;
    private final NewsUrlBuilder urlBuilder;
    private final AuditService auditService;

    public NewsCategoryService(NewsCategoryRepository categoryRepository,
                               NewsUrlBuilder urlBuilder,
                               AuditService auditService) {
        this.categoryRepository = categoryRepository;
        this.urlBuilder = urlBuilder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<AdminCategoryResponse> listForAdmin() {
        return categoryRepository.findAllByOrderBySortOrderAscNameAsc().stream().map(this::toAdminResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PublicCategoryResponse> listForPublic() {
        return categoryRepository.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(category -> new PublicCategoryResponse(
                        category.getSlug(),
                        category.getName(),
                        category.getDescription(),
                        urlBuilder.categoryPath(category),
                        urlBuilder.absoluteUrl(urlBuilder.categoryPath(category))))
                .toList();
    }

    @Transactional
    public AdminCategoryResponse create(CategoryUpsertRequest request) {
        String slug = request.slug() == null || request.slug().isBlank()
                ? Slugs.slugify(request.name())
                : Slugs.slugify(request.slug());
        if (slug.isBlank()) {
            throw ApiException.badRequest("invalid_slug", "A category slug could not be derived from the name.");
        }
        if (categoryRepository.existsBySlug(slug)) {
            throw ApiException.conflict("slug_taken", "Another category already uses this slug.");
        }

        NewsCategory category = new NewsCategory();
        category.setSlug(slug);
        category.setName(request.name().trim());
        category.setDescription(blankToNull(request.description()));
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        categoryRepository.save(category);

        auditService.record(AuditAction.NEWS_CATEGORY_CREATED, "news_category", category.getId().toString(),
                "Created category " + category.getSlug(), null, snapshot(category));
        return toAdminResponse(category);
    }

    @Transactional
    public AdminCategoryResponse update(UUID id, CategoryUpsertRequest request) {
        NewsCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Category"));
        Map<String, Object> before = snapshot(category);

        if (request.slug() != null && !request.slug().isBlank()) {
            String slug = Slugs.slugify(request.slug());
            if (slug.isBlank()) {
                throw ApiException.badRequest("invalid_slug", "A category slug may only contain letters, digits and hyphens.");
            }
            if (!slug.equals(category.getSlug()) && categoryRepository.existsBySlugAndIdNot(slug, id)) {
                throw ApiException.conflict("slug_taken", "Another category already uses this slug.");
            }
            category.setSlug(slug);
        }
        category.setName(request.name().trim());
        category.setDescription(blankToNull(request.description()));
        if (request.sortOrder() != null) {
            category.setSortOrder(request.sortOrder());
        }
        categoryRepository.save(category);

        auditService.record(AuditAction.NEWS_CATEGORY_UPDATED, "news_category", category.getId().toString(),
                "Updated category " + category.getSlug(), before, snapshot(category));
        return toAdminResponse(category);
    }

    private AdminCategoryResponse toAdminResponse(NewsCategory category) {
        return new AdminCategoryResponse(
                category.getId(),
                category.getSlug(),
                category.getName(),
                category.getDescription(),
                category.getSortOrder(),
                urlBuilder.categoryPath(category),
                category.getVersion());
    }

    private Map<String, Object> snapshot(NewsCategory category) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("slug", category.getSlug());
        snapshot.put("name", category.getName());
        snapshot.put("sortOrder", category.getSortOrder());
        return snapshot;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
