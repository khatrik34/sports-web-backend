package com.sportsweb.backend.cricket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InningsRepository extends JpaRepository<Innings, UUID> {

    List<Innings> findByMatchIdOrderByInningsNumberAsc(UUID matchId);

    long countByMatchId(UUID matchId);
}
