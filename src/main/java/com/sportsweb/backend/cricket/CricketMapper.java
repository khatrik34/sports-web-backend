package com.sportsweb.backend.cricket;

import com.sportsweb.backend.cricket.dto.AdminMatchResponse;
import com.sportsweb.backend.cricket.dto.AdminPlayerResponse;
import com.sportsweb.backend.cricket.dto.AdminStandingRow;
import com.sportsweb.backend.cricket.dto.AdminTeamResponse;
import com.sportsweb.backend.cricket.dto.AdminTournamentResponse;
import com.sportsweb.backend.cricket.dto.PublicMatch;
import com.sportsweb.backend.cricket.dto.PublicPlayer;
import com.sportsweb.backend.cricket.dto.PublicStandingRow;
import com.sportsweb.backend.cricket.dto.PublicTeam;
import com.sportsweb.backend.cricket.dto.PublicTeamRef;
import com.sportsweb.backend.cricket.dto.PublicTournament;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Entity to DTO mapping. Labels match what the shipped website renders: category labels are
 * title case ("International") and a match's {@code status} is the phase the UI filters on
 * (live / upcoming / recent).
 */
@Component
public class CricketMapper {

    private final CricketUrlBuilder urlBuilder;

    public CricketMapper(CricketUrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

    // ---------------------------------------------------------------- labels

    public static String categoryLabel(CricketCategory category) {
        if (category == null) {
            return null;
        }
        return switch (category) {
            case INTERNATIONAL -> "International";
            case LEAGUE -> "League";
            case DOMESTIC -> "Domestic";
        };
    }

    public static String phaseOf(MatchStatus status) {
        if (status == null) {
            return "upcoming";
        }
        return switch (status) {
            case LIVE -> "live";
            case SCHEDULED -> "upcoming";
            case COMPLETED, ABANDONED -> "recent";
        };
    }

    // ---------------------------------------------------------------- public

    public PublicTeamRef toTeamRef(Team team) {
        if (team == null) {
            return null;
        }
        String path = urlBuilder.teamPath(team.getSlug());
        return new PublicTeamRef(team.getId(), team.getSlug(), team.getName(), team.getShortName(),
                team.getFlag(), team.getLogoUrl(), categoryLabel(team.getCategory()), path,
                urlBuilder.absolute(path));
    }

    public PublicTeam toPublicTeam(Team team) {
        String path = urlBuilder.teamPath(team.getSlug());
        return new PublicTeam(team.getId(), team.getSlug(), team.getName(), team.getShortName(),
                categoryLabel(team.getCategory()), team.getCountryCode(), team.getFlag(),
                team.getSport(), team.getDescription(), team.getLogoUrl(), path,
                urlBuilder.absolute(path));
    }

    public PublicPlayer toPublicPlayer(Player player) {
        String path = urlBuilder.playerPath(player.getSlug());
        return new PublicPlayer(player.getId(), player.getSlug(), player.getFullName(),
                player.getCountry(), player.getRole(), player.getBattingStyle(), player.getBowlingStyle(),
                player.getBornOn(), player.getImageUrl(), player.getDescription(),
                toTeamRef(player.getTeam()), path, urlBuilder.absolute(path));
    }

    public PublicTournament toPublicTournament(Tournament tournament) {
        String path = urlBuilder.tournamentPath(tournament.getSlug());
        List<String> teamSlugs = tournament.getTeams().stream().map(Team::getSlug).toList();
        return new PublicTournament(tournament.getId(), tournament.getSlug(), tournament.getName(),
                tournament.getShortName(), tournament.getDescription(), tournament.getFormat().name(),
                categoryLabel(tournament.getCategory()), tournament.getSeason(), tournament.getHost(),
                tournament.getDatesText(), tournament.getStartDate(), tournament.getEndDate(),
                tournament.getStatus().name(), tournament.getLogoUrl(), teamSlugs, path,
                urlBuilder.absolute(path));
    }

    public PublicMatch toPublicMatch(Match match) {
        String path = urlBuilder.matchPath(match.getSlug());
        Tournament tournament = match.getTournament();
        return new PublicMatch(match.getId(), match.getSlug(),
                tournament == null ? null : tournament.getName(),
                tournament == null ? null : tournament.getSlug(),
                match.getFormat() == null ? null : match.getFormat().name(),
                categoryLabel(match.getCategory()), match.getVenue(), match.getStartTime(),
                phaseOf(match.getStatus()), match.getStatus() == null ? null : match.getStatus().name(),
                match.getMatchNumber(), toTeamRef(match.getTeam1()), toTeamRef(match.getTeam2()),
                match.getTeam1Score(), match.getTeam2Score(), match.getNote(), match.getResultText(),
                path, urlBuilder.absolute(path));
    }

    public PublicStandingRow toPublicStanding(Standing standing) {
        return new PublicStandingRow(standing.getPosition(), toTeamRef(standing.getTeam()),
                standing.getPlayed(), standing.getWon(), standing.getLost(), standing.getTied(),
                standing.getNoResult(), standing.getPoints(), standing.getNrr());
    }

    // ---------------------------------------------------------------- admin

    public AdminTeamResponse toAdminTeam(Team team) {
        return new AdminTeamResponse(team.getId(), team.getSlug(), team.getName(), team.getShortName(),
                categoryLabel(team.getCategory()), team.getCountryCode(), team.getFlag(), team.getSport(),
                team.getDescription(), team.getLogoUrl(), team.isActive(), team.getCreatedAt(),
                team.getUpdatedAt(), team.getVersion());
    }

    public AdminPlayerResponse toAdminPlayer(Player player) {
        Team team = player.getTeam();
        return new AdminPlayerResponse(player.getId(), player.getSlug(), player.getFullName(),
                team == null ? null : team.getId(), team == null ? null : team.getSlug(),
                team == null ? null : team.getName(), player.getCountry(), player.getRole(),
                player.getBattingStyle(), player.getBowlingStyle(), player.getBornOn(),
                player.getImageUrl(), player.getDescription(), player.isActive(), player.getCreatedAt(),
                player.getUpdatedAt(), player.getVersion());
    }

    public AdminTournamentResponse toAdminTournament(Tournament tournament) {
        List<Team> teams = List.copyOf(tournament.getTeams());
        return new AdminTournamentResponse(tournament.getId(), tournament.getSlug(), tournament.getName(),
                tournament.getShortName(), tournament.getDescription(), tournament.getFormat().name(),
                categoryLabel(tournament.getCategory()), tournament.getSeason(), tournament.getHost(),
                tournament.getDatesText(), tournament.getStartDate(), tournament.getEndDate(),
                tournament.getStatus().name(), tournament.getLogoUrl(), tournament.isActive(),
                teams.stream().map(Team::getId).toList(), teams.stream().map(Team::getSlug).toList(),
                tournament.getCreatedAt(), tournament.getUpdatedAt(), tournament.getVersion());
    }

    public AdminMatchResponse toAdminMatch(Match match) {
        Tournament tournament = match.getTournament();
        return new AdminMatchResponse(match.getId(), match.getSlug(),
                tournament == null ? null : tournament.getId(),
                tournament == null ? null : tournament.getName(),
                match.getTeam1().getId(), match.getTeam1().getName(),
                match.getTeam2().getId(), match.getTeam2().getName(),
                match.getFormat() == null ? null : match.getFormat().name(),
                categoryLabel(match.getCategory()), match.getVenue(), match.getStartTime(),
                match.getStatus() == null ? null : match.getStatus().name(), match.getMatchNumber(),
                match.getTeam1Score(), match.getTeam2Score(), match.getNote(), match.getResultText(),
                match.isVisible(), match.getCreatedAt(), match.getUpdatedAt(), match.getVersion());
    }

    public AdminStandingRow toAdminStanding(Standing standing) {
        return new AdminStandingRow(standing.getTeam().getId(), standing.getTeam().getSlug(),
                standing.getTeam().getName(), standing.getPosition(), standing.getPlayed(),
                standing.getWon(), standing.getLost(), standing.getTied(), standing.getNoResult(),
                standing.getPoints(), standing.getNrr());
    }

    public String normalizeSlug(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
