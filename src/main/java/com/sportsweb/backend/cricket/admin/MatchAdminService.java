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
import com.sportsweb.backend.cricket.Match;
import com.sportsweb.backend.cricket.MatchRepository;
import com.sportsweb.backend.cricket.MatchStatus;
import com.sportsweb.backend.cricket.Team;
import com.sportsweb.backend.cricket.TeamRepository;
import com.sportsweb.backend.cricket.Tournament;
import com.sportsweb.backend.cricket.TournamentRepository;
import com.sportsweb.backend.cricket.dto.AdminMatchResponse;
import com.sportsweb.backend.cricket.dto.MatchUpsertRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fixture administration. Editors enter scores by hand; this project does not call an external
 * live-score feed, and nothing here claims to.
 */
@Service
public class MatchAdminService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final CricketMapper mapper;
    private final AuditService auditService;

    public MatchAdminService(MatchRepository matchRepository,
                             TeamRepository teamRepository,
                             TournamentRepository tournamentRepository,
                             CricketMapper mapper,
                             AuditService auditService) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminMatchResponse> list(String status, String tournamentSlug, int page, int size) {
        Specification<Match> specification = (root, criteriaQuery, cb) -> cb.conjunction();
        if (status != null && !status.isBlank()) {
            MatchStatus state = parseStatus(status);
            specification = specification.and((root, criteriaQuery, cb) -> cb.equal(root.get("status"), state));
        }
        if (tournamentSlug != null && !tournamentSlug.isBlank()) {
            String slug = tournamentSlug.trim();
            specification = specification.and((root, criteriaQuery, cb) -> cb.equal(
                    root.join("tournament", jakarta.persistence.criteria.JoinType.LEFT).get("slug"), slug));
        }
        Page<Match> result = matchRepository.findAll(specification,
                Pageables.of(page, size, Sort.by(Sort.Direction.DESC, "startTime")));
        return PageResponse.from(result, mapper::toAdminMatch);
    }

    @Transactional(readOnly = true)
    public AdminMatchResponse get(UUID id) {
        return mapper.toAdminMatch(load(id));
    }

    @Transactional
    public AdminMatchResponse create(MatchUpsertRequest request) {
        Match match = new Match();
        match.setSlug(SlugResolver.forCreate(request.slug(), slugSource(request), matchRepository::existsBySlug));
        applyEditableFields(match, request, true);
        matchRepository.save(match);
        auditService.record(AuditAction.MATCH_CREATED, "cricket_match", match.getId().toString(),
                "Created fixture '" + match.getSlug() + "'", null, snapshot(match));
        return mapper.toAdminMatch(match);
    }

    @Transactional
    public AdminMatchResponse update(UUID id, MatchUpsertRequest request) {
        Match match = load(id);
        Map<String, Object> before = snapshot(match);
        match.setSlug(SlugResolver.forUpdate(request.slug(), match.getSlug(),
                candidate -> matchRepository.existsBySlugAndIdNot(candidate, id)));
        applyEditableFields(match, request, false);
        matchRepository.save(match);
        auditService.record(AuditAction.MATCH_UPDATED, "cricket_match", id.toString(),
                "Updated fixture '" + match.getSlug() + "'", before, snapshot(match));
        return mapper.toAdminMatch(match);
    }

    @Transactional
    public AdminMatchResponse setVisible(UUID id, boolean visible) {
        Match match = load(id);
        if (match.isVisible() == visible) {
            return mapper.toAdminMatch(match);
        }
        Map<String, Object> before = snapshot(match);
        match.setVisible(visible);
        matchRepository.save(match);
        auditService.record(AuditAction.MATCH_UPDATED, "cricket_match", id.toString(),
                (visible ? "Shown" : "Hidden") + " fixture '" + match.getSlug() + "'", before, snapshot(match));
        return mapper.toAdminMatch(match);
    }

    @Transactional
    public void delete(UUID id) {
        Match match = load(id);
        Map<String, Object> before = snapshot(match);
        matchRepository.delete(match);
        auditService.record(AuditAction.MATCH_DELETED, "cricket_match", id.toString(),
                "Deleted fixture '" + match.getSlug() + "'", before, null);
    }

    private void applyEditableFields(Match match, MatchUpsertRequest request, boolean creating) {
        Team team1 = resolveTeam(request.team1Id());
        Team team2 = resolveTeam(request.team2Id());
        if (team1.getId().equals(team2.getId())) {
            throw ApiException.badRequest("invalid_match", "A match needs two different teams.");
        }
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setTournament(resolveTournament(request.tournamentId()));
        match.setFormat(request.format() == null ? CricketFormat.T20 : request.format());
        match.setCategory(request.category() == null ? CricketCategory.INTERNATIONAL : request.category());
        match.setVenue(Texts.blankToNull(request.venue()));
        match.setStartTime(request.startTime());
        match.setStatus(request.status() == null ? MatchStatus.SCHEDULED : request.status());
        match.setMatchNumber(Texts.blankToNull(request.matchNumber()));
        match.setTeam1Score(Texts.blankToNull(request.team1Score()));
        match.setTeam2Score(Texts.blankToNull(request.team2Score()));
        match.setNote(Texts.blankToNull(request.note()));
        match.setResultText(Texts.blankToNull(request.resultText()));
        if (request.visible() != null) {
            match.setVisible(request.visible());
        } else if (creating) {
            match.setVisible(true);
        }
    }

    private String slugSource(MatchUpsertRequest request) {
        String team1Name = teamRepository.findById(request.team1Id()).map(Team::getName).orElse("match");
        String team2Name = teamRepository.findById(request.team2Id()).map(Team::getName).orElse("match");
        String year = request.startTime() == null ? "" : " " + request.startTime().atZone(java.time.ZoneOffset.UTC).getYear();
        return team1Name + " vs " + team2Name + year;
    }

    private Team resolveTeam(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> ApiException.badRequest("invalid_reference", "Team was not found."));
    }

    private Tournament resolveTournament(UUID tournamentId) {
        if (tournamentId == null) {
            return null;
        }
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> ApiException.badRequest("invalid_reference", "Series was not found."));
    }

    private MatchStatus parseStatus(String status) {
        try {
            return MatchStatus.valueOf(status.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("invalid_filter", "Unknown match status: " + status);
        }
    }

    private Match load(UUID id) {
        return matchRepository.findById(id).orElseThrow(() -> ApiException.notFound("Match"));
    }

    private Map<String, Object> snapshot(Match match) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("slug", match.getSlug());
        values.put("status", match.getStatus());
        values.put("visible", match.isVisible());
        values.put("startTime", match.getStartTime() == null ? null : match.getStartTime().toString());
        values.put("team1Score", match.getTeam1Score());
        values.put("team2Score", match.getTeam2Score());
        return values;
    }
}
