package com.sportsweb.backend.cricket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommentaryRepository extends JpaRepository<Commentary, UUID> {

    Page<Commentary> findByMatchIdOrderByCreatedAtDescIdDesc(UUID matchId, Pageable pageable);

    Optional<Commentary> findByIdAndMatchId(UUID id, UUID matchId);
}
