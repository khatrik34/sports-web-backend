package com.sportsweb.backend.cricket.publicapi;

import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.cricket.dto.PublicMatch;
import com.sportsweb.backend.cricket.dto.PublicPlayer;
import com.sportsweb.backend.cricket.dto.PublicStandingRow;
import com.sportsweb.backend.cricket.dto.PublicTeam;
import com.sportsweb.backend.cricket.dto.PublicTournament;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Anonymous endpoints that back the existing website pages: /teams, /players/{slug},
 * /cricket/series, /cricket/fixtures, /cricket/live-scores, /cricket/match/{slug}.
 */
@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public · Cricket")
public class CricketPublicController {

    private final CricketPublicService service;

    public CricketPublicController(CricketPublicService service) {
        this.service = service;
    }

    @GetMapping("/teams")
    @Operation(summary = "List active teams (paged)")
    public PageResponse<PublicTeam> teams(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "50") int size,
                                          @RequestParam(required = false) String q,
                                          @RequestParam(required = false) String category) {
        return service.teams(q, category, page, size);
    }

    @GetMapping("/teams/{slug}")
    @Operation(summary = "One active team by slug")
    public PublicTeam team(@PathVariable String slug) {
        return service.team(slug);
    }

    @GetMapping("/players")
    @Operation(summary = "List active players, optionally by team (paged)")
    public PageResponse<PublicPlayer> players(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "50") int size,
                                              @RequestParam(required = false) String q,
                                              @RequestParam(required = false) String team) {
        return service.players(q, team, page, size);
    }

    @GetMapping("/players/{slug}")
    @Operation(summary = "One active player by slug")
    public PublicPlayer player(@PathVariable String slug) {
        return service.player(slug);
    }

    @GetMapping("/tournaments")
    @Operation(summary = "List active series, earliest first")
    public List<PublicTournament> tournaments(@RequestParam(required = false) String q) {
        return service.tournaments(q);
    }

    @GetMapping("/tournaments/{slug}")
    @Operation(summary = "One active series by slug")
    public PublicTournament tournament(@PathVariable String slug) {
        return service.tournament(slug);
    }

    @GetMapping("/tournaments/{slug}/standings")
    @Operation(summary = "Points table for a series")
    public List<PublicStandingRow> standings(@PathVariable String slug) {
        return service.standings(slug);
    }

    @GetMapping("/matches")
    @Operation(summary = "Fixtures and results; filter mirrors the website's own filter values")
    public PageResponse<PublicMatch> matches(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(required = false) String filter,
                                             @RequestParam(required = false) String format,
                                             @RequestParam(required = false) String category,
                                             @RequestParam(required = false) String tournament,
                                             @RequestParam(required = false) String team,
                                             @RequestParam(required = false) String date) {
        return service.matches(filter, format, category, tournament, team, date, page, size);
    }

    @GetMapping("/matches/{slug}")
    @Operation(summary = "One visible fixture by slug")
    public PublicMatch match(@PathVariable String slug) {
        return service.match(slug);
    }
}
