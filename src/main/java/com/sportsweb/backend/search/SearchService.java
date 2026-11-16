package com.sportsweb.backend.search;

import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.common.util.Texts;
import com.sportsweb.backend.cricket.CricketUrlBuilder;
import com.sportsweb.backend.cricket.Match;
import com.sportsweb.backend.cricket.MatchRepository;
import com.sportsweb.backend.cricket.MatchSpecifications;
import com.sportsweb.backend.cricket.Player;
import com.sportsweb.backend.cricket.PlayerRepository;
import com.sportsweb.backend.cricket.PlayerSpecifications;
import com.sportsweb.backend.cricket.Team;
import com.sportsweb.backend.cricket.TeamRepository;
import com.sportsweb.backend.cricket.TeamSpecifications;
import com.sportsweb.backend.cricket.Tournament;
import com.sportsweb.backend.cricket.TournamentRepository;
import com.sportsweb.backend.cricket.TournamentSpecifications;
import com.sportsweb.backend.news.NewsArticle;
import com.sportsweb.backend.news.NewsArticleRepository;
import com.sportsweb.backend.news.NewsSpecifications;
import com.sportsweb.backend.news.NewsUrlBuilder;
import com.sportsweb.backend.search.dto.SearchResponse;
import com.sportsweb.backend.search.dto.SearchResultResponse;
import com.sportsweb.backend.videos.Video;
import com.sportsweb.backend.videos.VideoRepository;
import com.sportsweb.backend.videos.VideoSpecifications;
import com.sportsweb.backend.videos.VideoUrlBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The website's /search page needs one call instead of six. Each section keeps its own visibility
 * rules: drafts, archived articles, deactivated teams/players and hidden fixtures or videos can never
 * surface here.
 */
@Service
public class SearchService {

    private static final int MAX_PER_SECTION = 10;

    private final NewsArticleRepository newsRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final VideoRepository videoRepository;
    private final NewsUrlBuilder newsUrlBuilder;
    private final CricketUrlBuilder cricketUrlBuilder;
    private final VideoUrlBuilder videoUrlBuilder;

    public SearchService(NewsArticleRepository newsRepository,
                         TeamRepository teamRepository,
                         PlayerRepository playerRepository,
                         MatchRepository matchRepository,
                         TournamentRepository tournamentRepository,
                         VideoRepository videoRepository,
                         NewsUrlBuilder newsUrlBuilder,
                         CricketUrlBuilder cricketUrlBuilder,
                         VideoUrlBuilder videoUrlBuilder) {
        this.newsRepository = newsRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.tournamentRepository = tournamentRepository;
        this.videoRepository = videoRepository;
        this.newsUrlBuilder = newsUrlBuilder;
        this.cricketUrlBuilder = cricketUrlBuilder;
        this.videoUrlBuilder = videoUrlBuilder;
    }

    @Transactional(readOnly = true)
    public SearchResponse search(String query, int limit) {
        String term = Texts.blankToNull(query);
        if (term == null || term.length() < 2) {
            throw ApiException.badRequest("query_too_short", "Search for at least two characters.");
        }
        int perSection = Math.min(Math.max(limit, 1), MAX_PER_SECTION);
        Instant now = Instant.now();
        List<SearchResultResponse> results = new ArrayList<>();

        for (NewsArticle article : newsRepository.findAll(
                NewsSpecifications.publiclyVisible(now).and(NewsSpecifications.matchingText(term)),
                PageRequest.of(0, perSection, Sort.by(Sort.Direction.DESC, "publishedAt"))).getContent()) {
            add(results, "news", article.getId(), article.getTitle(), article.getSummary(),
                    newsUrlBuilder.path(article));
        }
        for (Match match : matchRepository.findAll(
                MatchSpecifications.visible().and(MatchSpecifications.matchingText(term)),
                PageRequest.of(0, perSection, Sort.by(Sort.Direction.DESC, "startTime"))).getContent()) {
            String title = match.getTeam1().getName() + " vs " + match.getTeam2().getName();
            String subtitle = match.getTournament() == null ? match.getVenue() : match.getTournament().getName();
            add(results, "match", match.getId(), title, subtitle, cricketUrlBuilder.matchPath(match.getSlug()));
        }
        for (Tournament tournament : tournamentRepository.findAll(
                TournamentSpecifications.active().and(TournamentSpecifications.matchingText(term)),
                PageRequest.of(0, perSection, Sort.by(Sort.Direction.DESC, "startDate"))).getContent()) {
            add(results, "series", tournament.getId(), tournament.getName(), tournament.getDatesText(),
                    cricketUrlBuilder.tournamentPath(tournament.getSlug()));
        }
        for (Team team : teamRepository.findAll(
                TeamSpecifications.active().and(TeamSpecifications.matchingText(term)),
                PageRequest.of(0, perSection, Sort.by("name"))).getContent()) {
            add(results, "team", team.getId(), team.getName(), team.getDescription(),
                    cricketUrlBuilder.teamPath(team.getSlug()));
        }
        for (Player player : playerRepository.findAll(
                PlayerSpecifications.active().and(PlayerSpecifications.matchingText(term)),
                PageRequest.of(0, perSection, Sort.by("fullName"))).getContent()) {
            String subtitle = player.getRole() == null ? player.getCountry() : player.getRole();
            add(results, "player", player.getId(), player.getFullName(), subtitle,
                    cricketUrlBuilder.playerPath(player.getSlug()));
        }
        for (Video video : videoRepository.findAll(
                VideoSpecifications.publiclyVisible(now).and(VideoSpecifications.matchingText(term)),
                PageRequest.of(0, perSection, Sort.by(Sort.Direction.DESC, "publishedAt"))).getContent()) {
            add(results, "video", video.getId(), video.getTitle(), video.getCategory(),
                    videoUrlBuilder.path(video.getSlug()));
        }
        return new SearchResponse(term, results.size(), results);
    }

    private void add(List<SearchResultResponse> results, String type, java.util.UUID id, String title,
                     String subtitle, String path) {
        results.add(new SearchResultResponse(type, id, title, subtitle, path,
                newsUrlBuilder.absoluteUrl(path)));
    }
}
