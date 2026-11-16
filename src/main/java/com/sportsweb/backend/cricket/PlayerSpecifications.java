package com.sportsweb.backend.cricket;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class PlayerSpecifications {

    private PlayerSpecifications() {
    }

    public static Specification<Player> active() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Player> matchingText(String text) {
        String pattern = "%" + text.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("fullName")), pattern),
                cb.like(cb.lower(root.get("slug")), pattern),
                cb.like(cb.lower(root.get("country")), pattern));
    }

    public static Specification<Player> inTeam(String teamSlug) {
        return (root, query, cb) -> cb.equal(
                root.join("team", JoinType.LEFT).get("slug"), teamSlug);
    }
}
