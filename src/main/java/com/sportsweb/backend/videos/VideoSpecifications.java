package com.sportsweb.backend.videos;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Locale;

public final class VideoSpecifications {

    private VideoSpecifications() {
    }

    public static Specification<Video> publiclyVisible(Instant now) {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("visible")),
                cb.isNotNull(root.get("publishedAt")),
                cb.lessThanOrEqualTo(root.get("publishedAt"), now));
    }

    public static Specification<Video> matchingText(String text) {
        String pattern = "%" + text.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("slug")), pattern));
    }

    public static Specification<Video> inCategoryLike(String category) {
        String pattern = "%" + category.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("category")), pattern);
    }

    public static Specification<Video> withVisibility(Boolean visible) {
        return (root, query, cb) -> visible == null ? cb.conjunction() : cb.equal(root.get("visible"), visible);
    }
}
