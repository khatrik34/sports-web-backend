package com.sportsweb.backend.news;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Locale;

/** Reusable query fragments. The public predicate is defined once, here. */
public final class NewsSpecifications {

    private NewsSpecifications() {
    }

    /**
     * The only definition of what the public may read: published, published in the past.
     * Everything else (drafts, archived, scheduled) is invisible.
     */
    public static Specification<NewsArticle> publiclyVisible(Instant now) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), NewsStatus.PUBLISHED),
                cb.isNotNull(root.get("publishedAt")),
                cb.lessThanOrEqualTo(root.get("publishedAt"), now));
    }

    /**
     * Search-engine indexability. A {@code robots} value containing {@code noindex} keeps the article
     * publicly readable but must be kept out of the sitemap feeds, otherwise the site asks crawlers to
     * index pages it explicitly told them not to index.
     */
    public static Specification<NewsArticle> indexable() {
        return (root, query, cb) -> cb.or(
                cb.isNull(root.get("robots")),
                cb.notLike(cb.lower(root.get("robots")), "%noindex%"));
    }

    public static Specification<NewsArticle> inCategory(String categorySlug) {
        return (root, query, cb) -> cb.equal(root.get("category").get("slug"), categorySlug);
    }

    public static Specification<NewsArticle> withStatus(NewsStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<NewsArticle> matchingText(String text) {
        String pattern = "%" + text.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("slug")), pattern),
                cb.like(cb.lower(root.get("summary")), pattern));
    }

    public static Specification<NewsArticle> featured() {
        return (root, query, cb) -> cb.isTrue(root.get("featured"));
    }
}
