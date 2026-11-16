package com.sportsweb.backend.cricket;

import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class TournamentSpecifications {

    private TournamentSpecifications() {
    }

    public static Specification<Tournament> active() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Tournament> withActiveState(Boolean active) {
        return (root, query, cb) -> active == null ? cb.conjunction() : cb.equal(root.get("active"), active);
    }

    public static Specification<Tournament> matchingText(String text) {
        String pattern = "%" + text.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("slug")), pattern));
    }
}
