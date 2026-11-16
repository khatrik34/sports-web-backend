package com.sportsweb.backend.cricket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID>, JpaSpecificationExecutor<Match> {

    Optional<Match> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}
