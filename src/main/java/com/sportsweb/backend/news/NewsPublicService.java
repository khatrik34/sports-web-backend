package com.sportsweb.backend.news;

import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.common.web.Pageables;
import com.sportsweb.backend.news.dto.PublicCategoryResponse;
import com.sportsweb.backend.news.dto.PublicNewsDetail;
import com.sportsweb.backend.news.dto.PublicNewsSummary;
import com.sportsweb.backend.news.dto.SitemapEntryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Anonymous read path. Every query starts from {@link NewsSpecifications#publiclyVisible},
 * so drafts, archived and future-dated articles cannot be returned even by mistake.
 */
@Service
public class NewsPublicService {

    private static final int MAX_LATEST = 50;

    private final NewsArticleRepository articleRepository;
    private final NewsMapper mapper;
    private final NewsCategoryService categoryService;
    private final NewsUrlBuilder urlBuilder;

    public NewsPublicService(NewsArticleRepository articleRepository,
                             NewsMapper mapper,
                             NewsCategoryService categoryService,
                             NewsUrlBuilder urlBuilder) {
        this.articleRepository = articleRepository;
        this.mapper = mapper;
        this.categoryService = categoryService;
        this.urlBuilder = urlBuilder;
    }

    @Transactional(readOnly = true)
    public PageResponse<PublicNewsSummary> list(String categorySlug, String query, int page, int size) {
        Instant now = Instant.now();
        Specification<NewsArticle> specification = NewsSpecifications.publiclyVisible(now);
        if (categorySlug != null && !categorySlug.isBlank()) {
            specification = specification.and(NewsSpecifications.inCategory(categorySlug.trim()));
        }
        if (query != null && !query.isBlank()) {
            specification = specification.and(NewsSpecifications.matchingText(query));
        }
        Page<NewsArticle> result = articleRepository.findAll(specification,
                Pageables.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt")));
        return PageResponse.from(result, mapper::toPublicSummary);
    }

    @Transactional(readOnly = true)
    public PublicNewsDetail getBySlug(String slug) {
        Instant now = Instant.now();
        NewsArticle article = articleRepository.findBySlug(slug)
                .filter(candidate -> candidate.isPubliclyVisible(now))
                .orElseThrow(() -> ApiException.notFound("Article"));
        return mapper.toPublicDetail(article);
    }

    @Transactional(readOnly = true)
    public List<PublicNewsSummary> latest(int limit, boolean featuredOnly) {
        int safeLimit = Math.min(Math.max(limit, 1), MAX_LATEST);
        Instant now = Instant.now();
        Specification<NewsArticle> specification = NewsSpecifications.publiclyVisible(now);
        if (featuredOnly) {
            specification = specification.and(NewsSpecifications.featured());
        }
        return articleRepository
                .findAll(specification, PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "publishedAt")))
                .getContent()
                .stream()
                .map(mapper::toPublicSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PublicCategoryResponse> categories() {
        return categoryService.listForPublic();
    }

    @Transactional(readOnly = true)
    public List<SitemapEntryResponse> sitemap() {
        Instant now = Instant.now();
        List<SitemapEntryResponse> entries = new ArrayList<>();
        articleRepository.findAllByStatusAndPublishedAtLessThanEqualOrderByPublishedAtDesc(NewsStatus.PUBLISHED, now)
                .forEach(article -> entries.add(new SitemapEntryResponse(
                        urlBuilder.absoluteUrl(urlBuilder.path(article)),
                        article.getUpdatedAt(),
                        "daily",
                        0.8)));
        for (PublicCategoryResponse category : categoryService.listForPublic()) {
            entries.add(new SitemapEntryResponse(category.url(), null, "weekly", 0.6));
        }
        return entries;
    }
}
