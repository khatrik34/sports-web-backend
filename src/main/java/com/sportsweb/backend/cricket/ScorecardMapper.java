package com.sportsweb.backend.cricket;

import com.sportsweb.backend.cricket.dto.BattingRowResponse;
import com.sportsweb.backend.cricket.dto.BowlingRowResponse;
import com.sportsweb.backend.cricket.dto.CommentaryResponse;
import com.sportsweb.backend.cricket.dto.InningsResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/** Shared mapping for scorecards and commentary, used by the admin and public services. */
@Component
public class ScorecardMapper {

    private final CricketMapper cricketMapper;

    public ScorecardMapper(CricketMapper cricketMapper) {
        this.cricketMapper = cricketMapper;
    }

    public InningsResponse toInnings(Innings innings) {
        List<BattingRowResponse> batting = innings.getBatting().stream()
                .map(row -> new BattingRowResponse(row.getId(), row.getPosition(), row.getBatter(),
                        row.getDismissal(), row.getRuns(), row.getBalls(), row.getFours(), row.getSixes(),
                        row.isNotOut()))
                .toList();
        List<BowlingRowResponse> bowling = innings.getBowling().stream()
                .map(row -> new BowlingRowResponse(row.getId(), row.getPosition(), row.getBowler(),
                        row.getOversText(), row.getMaidens(), row.getRunsConceded(), row.getWickets(),
                        economy(row.getRunsConceded(), row.getOversText())))
                .toList();
        return new InningsResponse(innings.getId(), innings.getInningsNumber(),
                cricketMapper.toTeamRef(innings.getBattingTeam()), innings.getRuns(), innings.getWickets(),
                innings.getOversText(), innings.isDeclared(), batting, bowling);
    }

    public CommentaryResponse toCommentary(Commentary entry) {
        return new CommentaryResponse(entry.getId(), entry.getOverText(), entry.getKind(), entry.getText(),
                entry.getCreatedAt());
    }

    /** Economy from a display overs string: "9.2" is 9 overs and 2 balls. */
    public static String economy(int runsConceded, String oversText) {
        int balls = ballsFromOvers(oversText);
        if (balls <= 0) {
            return null;
        }
        return String.format(Locale.ROOT, "%.2f", runsConceded * 6.0 / balls);
    }

    private static int ballsFromOvers(String oversText) {
        if (oversText == null || oversText.isBlank()) {
            return 0;
        }
        String[] parts = oversText.trim().split("\\.");
        try {
            int overs = Integer.parseInt(parts[0]);
            int balls = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return overs * 6 + Math.min(Math.max(balls, 0), 5);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
