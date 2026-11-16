package com.sportsweb.backend.cricket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StandingRepository extends JpaRepository<Standing, UUID> {

    List<Standing> findByTournamentIdOrderByPositionAscPlayedDesc(UUID tournamentId);

    void deleteByTournamentId(UUID tournamentId);

    boolean existsByTournamentIdAndTeamId(UUID tournamentId, UUID teamId);
}
