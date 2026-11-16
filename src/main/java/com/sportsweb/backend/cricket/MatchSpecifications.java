package com.sportsweb.backend.cricket;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

public final class MatchSpecifications {

    private MatchSpecifications() {
    }

    public static Specification<Match> visible() {
        return (root, query, cb) -> cb.isTrue(root.get("visible"));
    }

    public static Specification<Match> inPhase(String phase) {
        if (phase == null || phase.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return switch (phase) {
            case "live" -> (root, query, cb) -> cb.equal(root.get("status"), MatchStatus.LIVE);
            case "upcoming" -> (root, query, cb) -> cb.equal(root.get("status"), MatchStatus.SCHEDULED);
            case "recent" -> (root, query, cb) ->
                    root.get("status").in(List.of(MatchStatus.COMPLETED, MatchStatus.ABANDONED));
            default -> (root, query, cb) -> cb.conjunction();
        };
    }

    public static Specification<Match> withFormat(CricketFormat format) {
        return (root, query, cb) -> format == null ? cb.conjunction() : cb.equal(root.get("format"), format);
    }

    public static Specification<Match> inCategory(CricketCategory category) {
        return (root, query, cb) -> category == null ? cb.conjunction() : cb.equal(root.get("category"), category);
    }

    public static Specification<Match> inTournament(String tournamentSlug) {
        return (root, query, cb) -> tournamentSlug == null ? cb.conjunction()
                : cb.equal(root.join("tournament", JoinType.LEFT).get("slug"), tournamentSlug);
    }

    public static Specification<Match> involvingTeam(String teamSlug) {
        if (teamSlug == null || teamSlug.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.or(
                cb.equal(root.join("team1", JoinType.LEFT).get("slug"), teamSlug),
                cb.equal(root.join("team2", JoinType.LEFT).get("slug"), teamSlug));
    }

    public static Specification<Match> startingBetween(Instant from, Instant to) {
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("startTime"), from),
                cb.lessThan(root.get("startTime"), to));
    }
}
