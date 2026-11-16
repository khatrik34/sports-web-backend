package com.sportsweb.backend.cricket.admin;

import com.sportsweb.backend.common.audit.AuditAction;
import com.sportsweb.backend.common.audit.AuditService;
import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.common.util.Texts;
import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.common.web.Pageables;
import com.sportsweb.backend.cricket.BattingEntry;
import com.sportsweb.backend.cricket.BowlingEntry;
import com.sportsweb.backend.cricket.Commentary;
import com.sportsweb.backend.cricket.CommentaryRepository;
import com.sportsweb.backend.cricket.CricketMapper;
import com.sportsweb.backend.cricket.Innings;
import com.sportsweb.backend.cricket.InningsRepository;
import com.sportsweb.backend.cricket.ScorecardMapper;
import com.sportsweb.backend.cricket.Match;
import com.sportsweb.backend.cricket.MatchRepository;
import com.sportsweb.backend.cricket.Team;
import com.sportsweb.backend.cricket.TeamRepository;
import com.sportsweb.backend.cricket.dto.BattingRowRequest;
import com.sportsweb.backend.cricket.dto.BattingRowResponse;
import com.sportsweb.backend.cricket.dto.BowlingRowRequest;
import com.sportsweb.backend.cricket.dto.BowlingRowResponse;
import com.sportsweb.backend.cricket.dto.CommentaryRequest;
import com.sportsweb.backend.cricket.dto.CommentaryResponse;
import com.sportsweb.backend.cricket.dto.InningsRequest;
import com.sportsweb.backend.cricket.dto.InningsResponse;
import com.sportsweb.backend.cricket.dto.ScorecardUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Scorecards and commentary are entered by staff. This service owns both, because they are the
 * two tabs of the match centre and are always replaced/validated against the same fixture.
 */
@Service
public class MatchDetailAdminService {

    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;
    private final CommentaryRepository commentaryRepository;
    private final TeamRepository teamRepository;
    private final ScorecardMapper scorecardMapper;
    private final AuditService auditService;

    public MatchDetailAdminService(MatchRepository matchRepository,
                                   InningsRepository inningsRepository,
                                   CommentaryRepository commentaryRepository,
                                   TeamRepository teamRepository,
                                   ScorecardMapper scorecardMapper,
                                   AuditService auditService) {
        this.matchRepository = matchRepository;
        this.inningsRepository = inningsRepository;
        this.commentaryRepository = commentaryRepository;
        this.teamRepository = teamRepository;
        this.scorecardMapper = scorecardMapper;
        this.auditService = auditService;
    }

    // ---------------------------------------------------------------- scorecard

    @Transactional(readOnly = true)
    public List<InningsResponse> scorecard(UUID matchId) {
        Match match = loadMatch(matchId);
        return inningsRepository.findByMatchIdOrderByInningsNumberAsc(match.getId())
                .stream()
                .map(scorecardMapper::toInnings)
                .toList();
    }

    /** Full replacement: a scorecard is only meaningful as a whole innings set. */
    @Transactional
    public List<InningsResponse> replaceScorecard(UUID matchId, ScorecardUpdateRequest request) {
        Match match = loadMatch(matchId);
        List<Innings> existing = inningsRepository.findByMatchIdOrderByInningsNumberAsc(matchId);
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("innings", existing.size());

        List<Innings> replacements = new ArrayList<>();
        for (InningsRequest inningsRequest : request.innings()) {
            Innings innings = new Innings();
            innings.setMatch(match);
            innings.setInningsNumber(Math.max(inningsRequest.inningsNumber(), 1));
            innings.setBattingTeam(resolveBattingTeam(match, inningsRequest.battingTeamId()));
            innings.setRuns(Math.max(inningsRequest.runs(), 0));
            innings.setWickets(Math.min(Math.max(inningsRequest.wickets(), 0), 10));
            innings.setOversText(Texts.blankToNull(inningsRequest.overs()));
            innings.setDeclared(Boolean.TRUE.equals(inningsRequest.declared()));
            if (inningsRequest.batting() != null) {
                for (BattingRowRequest row : inningsRequest.batting()) {
                    innings.getBatting().add(toBatting(innings, row));
                }
            }
            if (inningsRequest.bowling() != null) {
                for (BowlingRowRequest row : inningsRequest.bowling()) {
                    innings.getBowling().add(toBowling(innings, row));
                }
            }
            replacements.add(innings);
        }

        inningsRepository.deleteAll(existing);
        inningsRepository.flush();
        inningsRepository.saveAll(replacements);
        inningsRepository.flush();

        auditService.record(AuditAction.SCORECARD_UPDATED, "cricket_match", matchId.toString(),
                "Replaced scorecard for '" + match.getSlug() + "' (" + replacements.size() + " innings)",
                before, Map.of("innings", replacements.size()));
        return replacements.stream().map(scorecardMapper::toInnings).toList();
    }

    // ---------------------------------------------------------------- commentary

    @Transactional(readOnly = true)
    public PageResponse<CommentaryResponse> commentary(UUID matchId, int page, int size) {
        loadMatch(matchId);
        Page<Commentary> result = commentaryRepository.findByMatchIdOrderByCreatedAtDescIdDesc(matchId,
                Pageables.of(page, size, Sort.unsorted()));
        return PageResponse.from(result, scorecardMapper::toCommentary);
    }

    @Transactional
    public CommentaryResponse addCommentary(UUID matchId, CommentaryRequest request) {
        Match match = loadMatch(matchId);
        Commentary entry = new Commentary();
        entry.setMatch(match);
        entry.setText(request.text().trim());
        entry.setOverText(Texts.blankToNull(request.over()));
        entry.setKind(Texts.blankToNull(request.kind()));
        commentaryRepository.save(entry);
        auditService.record(AuditAction.COMMENTARY_CREATED, "cricket_commentary", entry.getId().toString(),
                "Added commentary " + (entry.getOverText() == null ? "" : entry.getOverText() + " ")
                        + "for '" + match.getSlug() + "'", null, Map.of("kind", String.valueOf(entry.getKind())));
        return scorecardMapper.toCommentary(entry);
    }

    @Transactional
    public void deleteCommentary(UUID matchId, UUID entryId) {
        Commentary entry = commentaryRepository.findByIdAndMatchId(entryId, matchId)
                .orElseThrow(() -> ApiException.notFound("Commentary entry"));
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("over", entry.getOverText());
        before.put("kind", entry.getKind());
        commentaryRepository.delete(entry);
        auditService.record(AuditAction.COMMENTARY_DELETED, "cricket_commentary", entryId.toString(),
                "Deleted commentary entry", before, null);
    }

    private BattingEntry toBatting(Innings innings, BattingRowRequest row) {
        BattingEntry entry = new BattingEntry();
        entry.setInnings(innings);
        entry.setPosition(Math.max(row.position(), 0));
        entry.setBatter(row.batter().trim());
        entry.setDismissal(Texts.blankToNull(row.dismissal()));
        entry.setRuns(Math.max(row.runs(), 0));
        entry.setBalls(Math.max(row.balls(), 0));
        entry.setFours(Math.max(row.fours(), 0));
        entry.setSixes(Math.max(row.sixes(), 0));
        entry.setNotOut(Boolean.TRUE.equals(row.notOut()));
        return entry;
    }

    private BowlingEntry toBowling(Innings innings, BowlingRowRequest row) {
        BowlingEntry entry = new BowlingEntry();
        entry.setInnings(innings);
        entry.setPosition(Math.max(row.position(), 0));
        entry.setBowler(row.bowler().trim());
        entry.setOversText(Texts.blankToNull(row.overs()));
        entry.setMaidens(Math.max(row.maidens(), 0));
        entry.setRunsConceded(Math.max(row.runsConceded(), 0));
        entry.setWickets(Math.max(row.wickets(), 0));
        return entry;
    }

    private Team resolveBattingTeam(Match match, UUID battingTeamId) {
        if (battingTeamId == null) {
            return null;
        }
        if (!battingTeamId.equals(match.getTeam1().getId()) && !battingTeamId.equals(match.getTeam2().getId())) {
            throw ApiException.badRequest("invalid_reference",
                    "The batting team must be one of the two teams in this fixture.");
        }
        return teamRepository.findById(battingTeamId)
                .orElseThrow(() -> ApiException.badRequest("invalid_reference", "Team was not found."));
    }

    private Match loadMatch(UUID matchId) {
        return matchRepository.findById(matchId).orElseThrow(() -> ApiException.notFound("Match"));
    }
}
