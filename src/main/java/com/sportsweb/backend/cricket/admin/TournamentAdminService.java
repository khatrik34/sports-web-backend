package com.sportsweb.backend.cricket.admin;

import com.sportsweb.backend.common.audit.AuditAction;
import com.sportsweb.backend.common.audit.AuditService;
import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.common.util.SlugResolver;
import com.sportsweb.backend.common.util.Texts;
import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.common.web.Pageables;
import com.sportsweb.backend.cricket.CricketCategory;
import com.sportsweb.backend.cricket.CricketFormat;
import com.sportsweb.backend.cricket.CricketMapper;
import com.sportsweb.backend.cricket.Standing;
import com.sportsweb.backend.cricket.StandingRepository;
import com.sportsweb.backend.cricket.Team;
import com.sportsweb.backend.cricket.TeamRepository;
import com.sportsweb.backend.cricket.Tournament;
import com.sportsweb.backend.cricket.TournamentRepository;
import com.sportsweb.backend.cricket.TournamentSpecifications;
import com.sportsweb.backend.cricket.TournamentStatus;
import com.sportsweb.backend.cricket.dto.AdminStandingRow;
import com.sportsweb.backend.cricket.dto.AdminTournamentResponse;
import com.sportsweb.backend.cricket.dto.StandingRowRequest;
import com.sportsweb.backend.cricket.dto.StandingsUpdateRequest;
import com.sportsweb.backend.cricket.dto.TournamentUpsertRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Series/tournament administration, including the participating teams and the points table. */
@Service
public class TournamentAdminService {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final StandingRepository standingRepository;
    private final CricketMapper mapper;
    private final AuditService auditService;

    public TournamentAdminService(TournamentRepository tournamentRepository,
                                  TeamRepository teamRepository,
                                  StandingRepository standingRepository,
                                  CricketMapper mapper,
                                  AuditService auditService) {
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.standingRepository = standingRepository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminTournamentResponse> list(String query, Boolean active, int page, int size) {
        Specification<Tournament> specification = (root, criteriaQuery, cb) -> cb.conjunction();
        specification = specification.and(TournamentSpecifications.withActiveState(active));
        if (query != null && !query.isBlank()) {
            specification = specification.and(TournamentSpecifications.matchingText(query));
        }
        Page<Tournament> result = tournamentRepository.findAll(specification,
                Pageables.of(page, size, Sort.by(Sort.Direction.DESC, "startDate")));
        return PageResponse.from(result, mapper::toAdminTournament);
    }

    @Transactional(readOnly = true)
    public AdminTournamentResponse get(UUID id) {
        return mapper.toAdminTournament(load(id));
    }

    @Transactional
    public AdminTournamentResponse create(TournamentUpsertRequest request) {
        Tournament tournament = new Tournament();
        tournament.setSlug(SlugResolver.forCreate(request.slug(), request.name(), tournamentRepository::existsBySlug));
        applyEditableFields(tournament, request, true);
        tournamentRepository.save(tournament);
        auditService.record(AuditAction.TOURNAMENT_CREATED, "cricket_tournament", tournament.getId().toString(),
                "Created series '" + tournament.getName() + "'", null, snapshot(tournament));
        return mapper.toAdminTournament(tournament);
    }

    @Transactional
    public AdminTournamentResponse update(UUID id, TournamentUpsertRequest request) {
        Tournament tournament = load(id);
        Map<String, Object> before = snapshot(tournament);
        tournament.setSlug(SlugResolver.forUpdate(request.slug(), tournament.getSlug(),
                candidate -> tournamentRepository.existsBySlugAndIdNot(candidate, id)));
        applyEditableFields(tournament, request, false);
        tournamentRepository.save(tournament);
        auditService.record(AuditAction.TOURNAMENT_UPDATED, "cricket_tournament", id.toString(),
                "Updated series '" + tournament.getName() + "'", before, snapshot(tournament));
        return mapper.toAdminTournament(tournament);
    }

    @Transactional
    public AdminTournamentResponse setActive(UUID id, boolean active) {
        Tournament tournament = load(id);
        if (tournament.isActive() == active) {
            return mapper.toAdminTournament(tournament);
        }
        Map<String, Object> before = snapshot(tournament);
        tournament.setActive(active);
        tournamentRepository.save(tournament);
        auditService.record(AuditAction.TOURNAMENT_STATUS_CHANGED, "cricket_tournament", id.toString(),
                (active ? "Activated" : "Deactivated") + " series '" + tournament.getName() + "'",
                before, snapshot(tournament));
        return mapper.toAdminTournament(tournament);
    }

    @Transactional(readOnly = true)
    public List<AdminStandingRow> standings(UUID id) {
        Tournament tournament = load(id);
        return standingRepository.findByTournamentIdOrderByPositionAscPlayedDesc(tournament.getId())
                .stream()
                .map(mapper::toAdminStanding)
                .toList();
    }

    /** Replaces the whole table: a points table is always consistent as a whole. */
    @Transactional
    public List<AdminStandingRow> replaceStandings(UUID id, StandingsUpdateRequest request) {
        Tournament tournament = load(id);
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("rows", standingRepository.findByTournamentIdOrderByPositionAscPlayedDesc(id).size());

        Set<UUID> seen = new HashSet<>();
        List<Standing> replacements = new ArrayList<>();
        for (StandingRowRequest row : request.rows()) {
            if (!seen.add(row.teamId())) {
                throw ApiException.badRequest("duplicate_team", "A team appears more than once in the points table.");
            }
            Team team = teamRepository.findById(row.teamId())
                    .orElseThrow(() -> ApiException.badRequest("invalid_reference", "Team was not found."));
            Standing standing = new Standing();
            standing.setTournament(tournament);
            standing.setTeam(team);
            applyStanding(standing, row);
            replacements.add(standing);
        }

        standingRepository.deleteByTournamentId(id);
        standingRepository.flush();
        standingRepository.saveAll(replacements);

        auditService.record(AuditAction.TOURNAMENT_STANDINGS_UPDATED, "cricket_tournament", id.toString(),
                "Replaced points table for '" + tournament.getName() + "' (" + replacements.size() + " rows)",
                before, Map.of("rows", replacements.size()));
        return replacements.stream().map(mapper::toAdminStanding).toList();
    }

    private void applyStanding(Standing standing, StandingRowRequest row) {
        standing.setPosition(Math.max(row.position(), 0));
        standing.setPlayed(Math.max(row.played(), 0));
        standing.setWon(Math.max(row.won(), 0));
        standing.setLost(Math.max(row.lost(), 0));
        standing.setTied(Math.max(row.tied(), 0));
        standing.setNoResult(Math.max(row.noResult(), 0));
        standing.setPoints(Math.max(row.points(), 0));
        standing.setNrr(Texts.blankToNull(row.nrr()));
    }

    private void applyEditableFields(Tournament tournament, TournamentUpsertRequest request, boolean creating) {
        tournament.setName(request.name().trim());
        tournament.setShortName(Texts.blankToNull(request.shortName()));
        tournament.setDescription(Texts.blankToNull(request.description()));
        tournament.setFormat(request.format() == null ? CricketFormat.TEST : request.format());
        tournament.setCategory(request.category() == null ? CricketCategory.INTERNATIONAL : request.category());
        tournament.setSeason(Texts.blankToNull(request.season()));
        tournament.setHost(Texts.blankToNull(request.host()));
        tournament.setDatesText(Texts.blankToNull(request.datesText()));
        tournament.setStartDate(request.startDate());
        tournament.setEndDate(request.endDate());
        if (request.startDate() != null && request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw ApiException.badRequest("invalid_dates", "The end date cannot be before the start date.");
        }
        tournament.setStatus(request.status() == null ? TournamentStatus.UPCOMING : request.status());
        tournament.setLogoUrl(Texts.blankToNull(request.logoUrl()));
        if (request.active() != null) {
            tournament.setActive(request.active());
        } else if (creating) {
            tournament.setActive(true);
        }
        if (request.teamIds() != null) {
            Set<UUID> ids = new HashSet<>(request.teamIds());
            List<Team> teams = teamRepository.findAllById(ids);
            if (teams.size() != ids.size()) {
                throw ApiException.badRequest("invalid_reference", "One or more teams were not found.");
            }
            tournament.getTeams().clear();
            tournament.getTeams().addAll(teams);
        }
    }

    private Tournament load(UUID id) {
        return tournamentRepository.findById(id).orElseThrow(() -> ApiException.notFound("Series"));
    }

    private Map<String, Object> snapshot(Tournament tournament) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("slug", tournament.getSlug());
        values.put("name", tournament.getName());
        values.put("format", tournament.getFormat());
        values.put("status", tournament.getStatus());
        values.put("active", tournament.isActive());
        values.put("teamCount", tournament.getTeams().size());
        return values;
    }
}
