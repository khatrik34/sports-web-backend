package com.sportsweb.backend.cricket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface TournamentRepository extends JpaRepository<Tournament, UUID>, JpaSpecificationExecutor<Tournament> {

    Optional<Tournament> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}
