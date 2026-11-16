package com.sportsweb.backend.news;

import com.sportsweb.backend.news.dto.AdminNewsDetail;
import com.sportsweb.backend.news.dto.AdminNewsSeo;
import com.sportsweb.backend.news.dto.AdminNewsSummary;
import com.sportsweb.backend.news.dto.PublicNewsDetail;
import com.sportsweb.backend.news.dto.PublicNewsSummary;
import com.sportsweb.backend.news.dto.PublicSeo;
import org.springframework.stereotype.Component;

/**
 * Entity to DTO mapping. Admin and public records are separate types on purpose: it is not
 * possible to accidentally serialise an entity (and therefore an internal field) publicly.
 */
@Component
public class NewsMapper {

    private final NewsUrlBuilder urlBuilder;

    public NewsMapper(NewsUrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

    public AdminNewsSummary toAdminSummary(NewsArticle article) {
        return new AdminNewsSummary(
                article.getId(),
                article.getSlug(),
                article.getTitle(),
                article.getStatus(),
                categorySlug(article),
                categoryName(article),
                article.getAuthorDisplayName(),
                article.isFeatured(),
                urlBuilder.path(article),
                article.getPublishedAt(),
                article.getUpdatedAt());
    }

    public AdminNewsDetail toAdminDetail(NewsArticle article) {
        String path = urlBuilder.path(article);
        String url = urlBuilder.absoluteUrl(path);
        String metaTitle = firstNonBlank(article.getMetaTitle(), article.getTitle());
        String metaDescription = firstNonBlank(article.getMetaDescription(), article.getSummary());
        String ogTitle = firstNonBlank(article.getOgTitle(), metaTitle);
        String ogDescription = firstNonBlank(article.getOgDescription(), metaDescription);
        String ogImage = firstNonBlank(article.getOgImageUrl(), article.getCoverImageUrl());
        return new AdminNewsDetail(
                article.getId(),
                article.getSlug(),
                article.getTitle(),
                article.getSubtitle(),
                article.getSummary(),
                article.getBody(),
                article.getBodyFormat(),
                article.getStatus(),
                categorySlug(article),
                categoryName(article),
                article.getAuthorDisplayName(),
                article.getCoverImageUrl(),
                article.getCoverImageAlt(),
                article.isFeatured(),
                path,
                url,
                article.getPublishedAt(),
                article.getArchivedAt(),
                article.getCreatedAt(),
                article.getUpdatedAt(),
                article.getVersion(),
                new AdminNewsSeo(
                        metaTitle,
                        metaDescription,
                        article.getCanonicalPath() == null ? path : article.getCanonicalPath(),
                        url,
                        ogTitle,
                        ogDescription,
                        ogImage,
                        article.getRobots(),
                        article.getKeywords()));
    }

    public PublicNewsSummary toPublicSummary(NewsArticle article) {
        String path = urlBuilder.path(article);
        return new PublicNewsSummary(
                article.getSlug(),
                article.getTitle(),
                article.getSubtitle(),
                article.getSummary(),
                categorySlug(article),
                categoryName(article),
                article.getAuthorDisplayName(),
                article.getCoverImageUrl(),
                article.getCoverImageAlt(),
                article.isFeatured(),
                article.getPublishedAt(),
                article.getUpdatedAt(),
                path,
                urlBuilder.absoluteUrl(path));
    }

    public PublicNewsDetail toPublicDetail(NewsArticle article) {
        String path = urlBuilder.path(article);
        String url = urlBuilder.absoluteUrl(path);
        String metaTitle = firstNonBlank(article.getMetaTitle(), article.getTitle());
        String metaDescription = firstNonBlank(article.getMetaDescription(), article.getSummary());
        PublicSeo seo = new PublicSeo(
                metaTitle,
                metaDescription,
                url,
                article.getRobots(),
                article.getKeywords(),
                firstNonBlank(article.getOgTitle(), metaTitle),
                firstNonBlank(article.getOgDescription(), metaDescription),
                firstNonBlank(article.getOgImageUrl(), article.getCoverImageUrl()));
        return new PublicNewsDetail(
                article.getSlug(),
                article.getTitle(),
                article.getSubtitle(),
                article.getSummary(),
                article.getBody(),
                article.getBodyFormat(),
                categorySlug(article),
                categoryName(article),
                article.getAuthorDisplayName(),
                article.getCoverImageUrl(),
                article.getCoverImageAlt(),
                article.isFeatured(),
                article.getPublishedAt(),
                article.getUpdatedAt(),
                path,
                url,
                seo);
    }

    private String categorySlug(NewsArticle article) {
        return article.getCategory() == null ? null : article.getCategory().getSlug();
    }

    private String categoryName(NewsArticle article) {
        return article.getCategory() == null ? null : article.getCategory().getName();
    }

    private String firstNonBlank(String candidate, String fallback) {
        return candidate == null || candidate.isBlank() ? fallback : candidate;
    }
}
