package com.sportsweb.backend.cricket.publicapi;

import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.common.util.Texts;
import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.common.web.Pageables;
import com.sportsweb.backend.cricket.CricketCategory;
import com.sportsweb.backend.cricket.CricketFormat;
import com.sportsweb.backend.cricket.Commentary;
import com.sportsweb.backend.cricket.CommentaryRepository;
import com.sportsweb.backend.cricket.CricketMapper;
import com.sportsweb.backend.cricket.InningsRepository;
import com.sportsweb.backend.cricket.ScorecardMapper;
import com.sportsweb.backend.cricket.Match;
import com.sportsweb.backend.cricket.MatchRepository;
import com.sportsweb.backend.cricket.MatchSpecifications;
import com.sportsweb.backend.cricket.Player;
import com.sportsweb.backend.cricket.PlayerRepository;
import com.sportsweb.backend.cricket.PlayerSpecifications;
import com.sportsweb.backend.cricket.StandingRepository;
import com.sportsweb.backend.cricket.Team;
import com.sportsweb.backend.cricket.TeamRepository;
import com.sportsweb.backend.cricket.TeamSpecifications;
import com.sportsweb.backend.cricket.Tournament;
import com.sportsweb.backend.cricket.TournamentRepository;
import com.sportsweb.backend.cricket.TournamentSpecifications;
import com.sportsweb.backend.cricket.TournamentStatus;
import com.sportsweb.backend.cricket.dto.CommentaryResponse;
import com.sportsweb.backend.cricket.dto.InningsResponse;
import com.sportsweb.backend.cricket.dto.PublicMatch;
import com.sportsweb.backend.cricket.dto.PublicPlayer;
import com.sportsweb.backend.cricket.dto.PublicRankings;
import com.sportsweb.backend.cricket.dto.PublicStandingRow;
import com.sportsweb.backend.cricket.dto.PublicTeam;
import com.sportsweb.backend.cricket.dto.PublicTournament;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

/**
 * Anonymous read path for teams, players, series and fixtures. Everything here is limited to
 * active/inactive filters an editor controls: deactivated teams, inactive players and hidden
 * fixtures never appear.
 */
@Service
public class CricketPublicService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final StandingRepository standingRepository;
    private final InningsRepository inningsRepository;
    private final CommentaryRepository commentaryRepository;
    private final ScorecardMapper scorecardMapper;
    private final CricketMapper mapper;

    public CricketPublicService(TeamRepository teamRepository,
                                PlayerRepository playerRepository,
                                TournamentRepository tournamentRepository,
                                MatchRepository matchRepository,
                                StandingRepository standingRepository,
                                InningsRepository inningsRepository,
                                CommentaryRepository commentaryRepository,
                                ScorecardMapper scorecardMapper,
                                CricketMapper mapper) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
        this.standingRepository = standingRepository;
        this.inningsRepository = inningsRepository;
        this.commentaryRepository = commentaryRepository;
        this.scorecardMapper = scorecardMapper;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------------- teams

    @Transactional(readOnly = true)
    public PageResponse<PublicTeam> teams(String query, String category, int page, int size) {
        Specification<Team> specification = TeamSpecifications.active()
                .and(TeamSpecifications.inCategory(parseCategory(category)));
        if (query != null && !query.isBlank()) {
            specification = specification.and(TeamSpecifications.matchingText(query));
        }
        Page<Team> result = teamRepository.findAll(specification, Pageables.of(page, size, Sort.by("name")));
        return PageResponse.from(result, mapper::toPublicTeam);
    }

    @Transactional(readOnly = true)
    public PublicTeam team(String slug) {
        Team team = teamRepository.findBySlug(slug)
                .filter(Team::isActive)
                .orElseThrow(() -> ApiException.notFound("Team"));
        return mapper.toPublicTeam(team);
    }

    // ---------------------------------------------------------------- players

    @Transactional(readOnly = true)
    public PageResponse<PublicPlayer> players(String query, String teamSlug, int page, int size) {
        Specification<Player> specification = PlayerSpecifications.active();
        if (query != null && !query.isBlank()) {
            specification = specification.and(PlayerSpecifications.matchingText(query));
        }
        if (teamSlug != null && !teamSlug.isBlank()) {
            specification = specification.and(PlayerSpecifications.inTeam(teamSlug.trim()));
        }
        Page<Player> result = playerRepository.findAll(specification, Pageables.of(page, size, Sort.by("fullName")));
        return PageResponse.from(result, mapper::toPublicPlayer);
    }

    @Transactional(readOnly = true)
    public PublicPlayer player(String slug) {
        Player player = playerRepository.findBySlug(slug)
                .filter(Player::isActive)
                .orElseThrow(() -> ApiException.notFound("Player"));
        return mapper.toPublicPlayer(player);
    }

    // ---------------------------------------------------------------- series

    @Transactional(readOnly = true)
    public List<PublicTournament> tournaments(String query) {
        Specification<Tournament> specification = TournamentSpecifications.active();
        if (query != null && !query.isBlank()) {
            specification = specification.and(TournamentSpecifications.matchingText(query));
        }
        return tournamentRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "startDate"))
                .stream()
                .map(mapper::toPublicTournament)
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicTournament tournament(String slug) {
        Tournament tournament = tournamentRepository.findBySlug(slug)
                .filter(Tournament::isActive)
                .orElseThrow(() -> ApiException.notFound("Series"));
        return mapper.toPublicTournament(tournament);
    }

    @Transactional(readOnly = true)
    public List<PublicStandingRow> standings(String slug) {
        Tournament tournament = tournamentRepository.findBySlug(slug)
                .filter(Tournament::isActive)
                .orElseThrow(() -> ApiException.notFound("Series"));
        return standingRepository.findByTournamentIdOrderByPositionAscPlayedDesc(tournament.getId())
                .stream()
                .map(mapper::toPublicStanding)
                .toList();
    }

    /**
     * The /rankings page reuses the points table. Prefer the ongoing series for the requested
     * format, otherwise the most recently started one; 404 when no series exists yet.
     */
    @Transactional(readOnly = true)
    public PublicRankings rankings(String format) {
        CricketFormat parsed = parseFormat(format);
        Specification<Tournament> specification = TournamentSpecifications.active();
        if (parsed != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("format"), parsed));
        }
        List<Tournament> candidates = tournamentRepository.findAll(specification,
                Sort.by(Sort.Order.desc("startDate").nullsLast(), Sort.Order.desc("updatedAt")));
        Tournament chosen = candidates.stream()
                .filter(candidate -> candidate.getStatus() == TournamentStatus.ONGOING)
                .findFirst()
                .orElse(candidates.isEmpty() ? null : candidates.get(0));
        if (chosen == null) {
            throw ApiException.notFound("Rankings");
        }
        List<PublicStandingRow> rows = standingRepository
                .findByTournamentIdOrderByPositionAscPlayedDesc(chosen.getId())
                .stream()
                .map(mapper::toPublicStanding)
                .toList();
        return new PublicRankings(chosen.getId(), chosen.getName(), chosen.getSlug(),
                chosen.getFormat().name(), CricketMapper.categoryLabel(chosen.getCategory()),
                chosen.getStatus().name(), rows);
    }

    // ---------------------------------------------------------------- fixtures

    /**
     * Mirrors the website's own filter vocabulary: {@code ?filter=live|upcoming|recent} for the
     * phase, or {@code ?filter=international|domestic|league} for the category, plus optional
     * format, tournament slug, team slug and a single UTC date.
     */
    @Transactional(readOnly = true)
    public PageResponse<PublicMatch> matches(String filter, String format, String category, String tournament,
                                             String team, String date, int page, int size) {
        String phase = null;
        String categoryFilter = category;
        if (filter != null && !filter.isBlank()) {
            String normalized = filter.trim().toLowerCase(Locale.ROOT);
            switch (normalized) {
                case "all" -> {
                }
                case "live", "upcoming", "recent" -> phase = normalized;
                case "international", "domestic", "league" -> categoryFilter = normalized;
                default -> throw ApiException.badRequest("invalid_filter", "Unknown filter: " + filter);
            }
        }

        Specification<Match> specification = MatchSpecifications.visible()
                .and(MatchSpecifications.inPhase(phase))
                .and(MatchSpecifications.withFormat(parseFormat(format)))
                .and(MatchSpecifications.inCategory(parseCategory(categoryFilter)))
                .and(MatchSpecifications.inTournament(Texts.blankToNull(tournament)))
                .and(MatchSpecifications.involvingTeam(Texts.blankToNull(team)));

        if (date != null && !date.isBlank()) {
            LocalDate day;
            try {
                day = LocalDate.parse(date.trim());
            } catch (DateTimeParseException ex) {
                throw ApiException.badRequest("invalid_date", "Date must use the ISO format yyyy-MM-dd.");
            }
            Instant from = day.atStartOfDay(ZoneOffset.UTC).toInstant();
            specification = specification.and(MatchSpecifications.startingBetween(from, from.plus(Duration.ofDays(1))));
        }

        Sort sort = "live".equals(phase) || "upcoming".equals(phase)
                ? Sort.by(Sort.Direction.ASC, "startTime")
                : Sort.by(Sort.Direction.DESC, "startTime");
        Page<Match> result = matchRepository.findAll(specification, Pageables.of(page, size, sort));
        return PageResponse.from(result, mapper::toPublicMatch);
    }

    @Transactional(readOnly = true)
    public PublicMatch match(String slug) {
        return mapper.toPublicMatch(visibleMatch(slug));
    }

    @Transactional(readOnly = true)
    public List<InningsResponse> scorecard(String slug) {
        Match match = visibleMatch(slug);
        return inningsRepository.findByMatchIdOrderByInningsNumberAsc(match.getId())
                .stream()
                .map(scorecardMapper::toInnings)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentaryResponse> commentary(String slug, int page, int size) {
        Match match = visibleMatch(slug);
        Page<Commentary> result = commentaryRepository.findByMatchIdOrderByCreatedAtDescIdDesc(match.getId(),
                Pageables.of(page, size, Sort.unsorted()));
        return PageResponse.from(result, scorecardMapper::toCommentary);
    }

    private Match visibleMatch(String slug) {
        return matchRepository.findBySlug(slug)
                .filter(Match::isVisible)
                .orElseThrow(() -> ApiException.notFound("Match"));
    }

    // ---------------------------------------------------------------- parsing

    private CricketCategory parseCategory(String value) {
        String candidate = Texts.blankToNull(value);
        if (candidate == null) {
            return null;
        }
        try {
            return CricketCategory.valueOf(candidate.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("invalid_filter", "Unknown category: " + value);
        }
    }

    private CricketFormat parseFormat(String value) {
        String candidate = Texts.blankToNull(value);
        if (candidate == null) {
            return null;
        }
        try {
            return CricketFormat.valueOf(candidate.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("invalid_filter", "Unknown format: " + value);
        }
    }
}
