package com.sportsweb.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.sportsweb.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CricketApiTest extends AbstractIntegrationTest {

    // ---------------------------------------------------------------- fixtures

    private JsonNode createTeam(String token, String slug, String name, boolean active) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("slug", slug);
        payload.put("name", name);
        payload.put("shortName", name.length() >= 3 ? name.substring(0, 3).toUpperCase() : name.toUpperCase());
        payload.put("category", "INTERNATIONAL");
        payload.put("countryCode", "IN");
        payload.put("flag", "X");
        payload.put("description", "A team used by tests.");
        payload.put("active", active);
        return postExpectingCreated("/api/v1/admin/teams", token, payload);
    }

    private JsonNode createTournament(String token, String slug, List<UUID> teamIds) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("slug", slug);
        payload.put("name", "Series " + slug);
        payload.put("shortName", "SER");
        payload.put("description", "A series used by tests.");
        payload.put("format", "TEST");
        payload.put("category", "INTERNATIONAL");
        payload.put("datesText", "2026");
        payload.put("status", "ONGOING");
        payload.put("teamIds", teamIds.stream().map(UUID::toString).toList());
        return postExpectingCreated("/api/v1/admin/tournaments", token, payload);
    }

    private JsonNode createMatch(String token, UUID team1, UUID team2, UUID tournamentId,
                                 String state, Instant startTime, boolean visible) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("slug", uniqueSlug("match"));
        payload.put("team1Id", team1.toString());
        payload.put("team2Id", team2.toString());
        payload.put("tournamentId", tournamentId == null ? null : tournamentId.toString());
        payload.put("format", "T20I");
        payload.put("category", "INTERNATIONAL");
        payload.put("venue", "Test Ground");
        payload.put("startTime", startTime.toString());
        payload.put("status", state);
        payload.put("team1Score", "150/3 (20)");
        payload.put("team2Score", "148/8 (20)");
        payload.put("note", "Test fixture");
        payload.put("visible", visible);
        return postExpectingCreated("/api/v1/admin/matches", token, payload);
    }

    private JsonNode createPlayer(String token, UUID teamId, String name) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("slug", uniqueSlug("player"));
        payload.put("fullName", name);
        payload.put("teamId", teamId.toString());
        payload.put("country", "India");
        payload.put("role", "Batter");
        payload.put("battingStyle", "Right-handed");
        payload.put("bowlingStyle", "Right-arm medium");
        return postExpectingCreated("/api/v1/admin/players", token, payload);
    }

    private static UUID id(JsonNode node) {
        return UUID.fromString(node.get("id").asText());
    }

    // ---------------------------------------------------------------- public teams

    @Test
    void publicTeamEndpointsAreAnonymousAndHideDeactivatedTeams() throws Exception {
        String token = adminToken();
        String visibleSlug = uniqueSlug("visible");
        String hiddenSlug = uniqueSlug("hidden");
        createTeam(token, visibleSlug, "Visible XI", true);
        createTeam(token, hiddenSlug, "Hidden XI", false);

        JsonNode page = getJson("/api/v1/public/teams?size=100", null);
        List<String> slugs = new ArrayList<>();
        page.get("content").forEach(team -> slugs.add(team.get("slug").asText()));
        assertThat(slugs).contains(visibleSlug).doesNotContain(hiddenSlug);
        assertThat(page.get("page").asInt()).isZero();

        JsonNode team = getJson("/api/v1/public/teams/" + visibleSlug, null);
        assertThat(team.get("path").asText()).isEqualTo("/teams/" + visibleSlug);
        assertThat(team.get("url").asText()).endsWith("/teams/" + visibleSlug);
        assertThat(team.get("category").asText()).isEqualTo("International");

        performExpectingStatus(get("/api/v1/public/teams/" + hiddenSlug), 404);
    }

    @Test
    void slugIsDerivedFromTheNameAndUpdatesCannotStealAnotherSlug() throws Exception {
        String token = adminToken();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "West Indies");
        payload.put("shortName", "WI");
        JsonNode created = postExpectingCreated("/api/v1/admin/teams", token, payload);
        assertThat(created.get("slug").asText()).startsWith("west-indies");

        String otherSlug = uniqueSlug("other");
        JsonNode other = createTeam(token, otherSlug, "Other XI", true);

        Map<String, Object> clash = new LinkedHashMap<>();
        clash.put("slug", "west-indies");
        clash.put("name", "Other XI Renamed");
        clash.put("shortName", "OTH");
        JsonNode conflict = performExpectingStatus(put("/api/v1/admin/teams/" + other.get("id").asText())
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(clash)), 409);
        assertThat(conflict.get("code").asText()).isEqualTo("slug_taken");
    }

    // ---------------------------------------------------------------- permissions

    @Test
    void reporterCanReadCricketButNotWriteIt() throws Exception {
        String reporter = tokenFor(reporterEmail());
        getJson("/api/v1/admin/teams", reporter);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "Forbidden XI");
        payload.put("shortName", "FOR");
        performExpectingStatus(post("/api/v1/admin/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer(reporter))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(payload)), 403);

        // The same payload works for an editor-level account.
        String editor = tokenFor(editorEmail());
        JsonNode created = postExpectingCreated("/api/v1/admin/teams", editor, payload);
        assertThat(created.get("slug").asText()).startsWith("forbidden-xi");
    }

    @Test
    void unknownTeamReferenceIsRejected() throws Exception {
        String token = adminToken();
        JsonNode team = createTeam(token, uniqueSlug("ref"), "Reference XI", true);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("team1Id", team.get("id").asText());
        payload.put("team2Id", UUID.randomUUID().toString());
        payload.put("startTime", Instant.now().plus(1, ChronoUnit.DAYS).toString());
        JsonNode error = performExpectingStatus(post("/api/v1/admin/matches")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(payload)), 400);
        assertThat(error.get("code").asText()).isEqualTo("invalid_reference");
    }

    // ---------------------------------------------------------------- fixtures

    @Test
    void fixturesAreFilteredByThePhasesTheWebsiteUses() throws Exception {
        String token = adminToken();
        JsonNode team1 = createTeam(token, uniqueSlug("t1"), "Alpha XI", true);
        JsonNode team2 = createTeam(token, uniqueSlug("t2"), "Beta XI", true);

        JsonNode live = createMatch(token, id(team1), id(team2), null, "LIVE",
                Instant.now().minus(1, ChronoUnit.HOURS), true);
        JsonNode upcoming = createMatch(token, id(team1), id(team2), null, "SCHEDULED",
                Instant.now().plus(2, ChronoUnit.DAYS), true);
        JsonNode recent = createMatch(token, id(team1), id(team2), null, "COMPLETED",
                Instant.now().minus(3, ChronoUnit.DAYS), true);

        assertThat(live.get("status").asText()).isEqualTo("LIVE");
        assertThat(live.get("id").asText()).isNotBlank();

        JsonNode livePage = getJson("/api/v1/public/matches?filter=live&size=100", null);
        assertThat(livePage.get("content").findValuesAsText("slug")).contains(live.get("slug").asText());
        livePage.get("content").forEach(match -> assertThat(match.get("status").asText()).isEqualTo("live"));
        livePage.get("content").forEach(match -> assertThat(match.get("state").asText()).isEqualTo("LIVE"));

        JsonNode upcomingPage = getJson("/api/v1/public/matches?filter=upcoming&size=100", null);
        assertThat(upcomingPage.get("content").findValuesAsText("slug")).contains(upcoming.get("slug").asText());
        upcomingPage.get("content").forEach(match -> assertThat(match.get("status").asText()).isEqualTo("upcoming"));

        JsonNode recentPage = getJson("/api/v1/public/matches?filter=recent&size=100", null);
        assertThat(recentPage.get("content").findValuesAsText("slug")).contains(recent.get("slug").asText());
        recentPage.get("content").forEach(match -> assertThat(match.get("status").asText()).isEqualTo("recent"));

        // category is also a valid filter value on the same parameter, exactly like the website
        JsonNode internationalPage = getJson("/api/v1/public/matches?filter=international&size=100", null);
        assertThat(internationalPage.get("content").findValuesAsText("slug"))
                .contains(live.get("slug").asText(), upcoming.get("slug").asText());

        JsonNode unknown = performExpectingStatus(get("/api/v1/public/matches?filter=nonsense"), 400);
        assertThat(unknown.get("code").asText()).isEqualTo("invalid_filter");
    }

    @Test
    void matchDetailCarriesBothTeamsAndTheWebsitePath() throws Exception {
        String token = adminToken();
        JsonNode team1 = createTeam(token, uniqueSlug("home"), "Home XI", true);
        JsonNode team2 = createTeam(token, uniqueSlug("away"), "Away XI", true);
        JsonNode match = createMatch(token, id(team1), id(team2), null, "LIVE",
                Instant.now().minus(2, ChronoUnit.HOURS), true);

        JsonNode detail = getJson("/api/v1/public/matches/" + match.get("slug").asText(), null);
        assertThat(detail.get("status").asText()).isEqualTo("live");
        assertThat(detail.get("team1").get("slug").asText()).isEqualTo(team1.get("slug").asText());
        assertThat(detail.get("team2").get("slug").asText()).isEqualTo(team2.get("slug").asText());
        assertThat(detail.get("path").asText()).isEqualTo("/cricket/match/" + match.get("slug").asText());
        assertThat(detail.get("venue").asText()).isEqualTo("Test Ground");
    }

    @Test
    void hiddenFixturesNeverReachThePublicApi() throws Exception {
        String token = adminToken();
        JsonNode team1 = createTeam(token, uniqueSlug("h1"), "Hidden Home", true);
        JsonNode team2 = createTeam(token, uniqueSlug("h2"), "Hidden Away", true);
        JsonNode hidden = createMatch(token, id(team1), id(team2), null, "LIVE",
                Instant.now().minus(1, ChronoUnit.HOURS), false);

        performExpectingStatus(get("/api/v1/public/matches/" + hidden.get("slug").asText()), 404);
        JsonNode page = getJson("/api/v1/public/matches?size=100", null);
        assertThat(page.get("content").findValuesAsText("slug")).doesNotContain(hidden.get("slug").asText());
        // the admin API still sees it
        JsonNode adminPage = getJson("/api/v1/admin/matches?size=100", token);
        assertThat(adminPage.get("content").findValuesAsText("slug")).contains(hidden.get("slug").asText());
    }

    // ---------------------------------------------------------------- series

    @Test
    void seriesExposeTeamSlugsAndTheirWebsitePath() throws Exception {
        String token = adminToken();
        JsonNode team1 = createTeam(token, uniqueSlug("s1"), "Series Team One", true);
        JsonNode team2 = createTeam(token, uniqueSlug("s2"), "Series Team Two", true);
        String slug = uniqueSlug("series");
        JsonNode tournament = createTournament(token, slug, List.of(id(team1), id(team2)));

        JsonNode publicSeries = getJson("/api/v1/public/tournaments/" + slug, null);
        List<String> seriesTeamSlugs = new ArrayList<>();
        publicSeries.get("teamSlugs").forEach(node -> seriesTeamSlugs.add(node.asText()));
        assertThat(seriesTeamSlugs)
                .containsExactlyInAnyOrder(team1.get("slug").asText(), team2.get("slug").asText());
        assertThat(publicSeries.get("path").asText()).isEqualTo("/cricket/series/" + slug);
        assertThat(publicSeries.get("format").asText()).isEqualTo("TEST");

        JsonNode listed = getJson("/api/v1/public/tournaments", null);
        assertThat(listed.findValuesAsText("slug")).contains(slug);

        // deactivating the series removes it from the public API but not from admin
        mockMvc.perform(post("/api/v1/admin/tournaments/" + tournament.get("id").asText() + "/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
        performExpectingStatus(get("/api/v1/public/tournaments/" + slug), 404);
        getJson("/api/v1/admin/tournaments/" + tournament.get("id").asText(), token);
    }

    @Test
    void pointsTableIsReplacedAsAWholeAndServedOrdered() throws Exception {
        String token = adminToken();
        JsonNode team1 = createTeam(token, uniqueSlug("p1"), "Points One", true);
        JsonNode team2 = createTeam(token, uniqueSlug("p2"), "Points Two", true);
        String slug = uniqueSlug("points");
        JsonNode tournament = createTournament(token, slug, List.of(id(team1), id(team2)));
        String path = "/api/v1/admin/tournaments/" + tournament.get("id").asText() + "/standings";

        Map<String, Object> second = new LinkedHashMap<>();
        second.put("teamId", team2.get("id").asText());
        second.put("position", 2);
        second.put("played", 8);
        second.put("won", 3);
        second.put("lost", 5);
        second.put("points", 36);
        second.put("nrr", "37.50%");

        Map<String, Object> first = new LinkedHashMap<>();
        first.put("teamId", team1.get("id").asText());
        first.put("position", 1);
        first.put("played", 8);
        first.put("won", 6);
        first.put("lost", 2);
        first.put("points", 72);
        first.put("nrr", "75.00%");

        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("rows", List.of(second, first)))))
                .andExpect(status().isOk());

        JsonNode standings = getJson("/api/v1/public/tournaments/" + slug + "/standings", null);
        assertThat(standings).hasSize(2);
        assertThat(standings.get(0).get("position").asInt()).isEqualTo(1);
        assertThat(standings.get(0).get("team").get("slug").asText()).isEqualTo(team1.get("slug").asText());
        assertThat(standings.get(0).get("nrr").asText()).isEqualTo("75.00%");
        assertThat(standings.get(0).get("points").asInt()).isEqualTo(72);

        // replacing with a single row drops the other one
        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("rows", List.of(first)))))
                .andExpect(status().isOk());
        assertThat(getJson("/api/v1/public/tournaments/" + slug + "/standings", null)).hasSize(1);

        // duplicate teams inside one replacement are rejected
        Map<String, Object> duplicate = new LinkedHashMap<>(first);
        JsonNode error = performExpectingStatus(put(path)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("rows", List.of(first, duplicate)))), 400);
        assertThat(error.get("code").asText()).isEqualTo("duplicate_team");
    }

    // ---------------------------------------------------------------- players

    @Test
    void playersCanBeListedByTeamAndArePublic() throws Exception {
        String token = adminToken();
        JsonNode teamA = createTeam(token, uniqueSlug("pa"), "Player Team A", true);
        JsonNode teamB = createTeam(token, uniqueSlug("pb"), "Player Team B", true);
        JsonNode playerA = createPlayer(token, id(teamA), "Player Alpha");
        createPlayer(token, id(teamB), "Player Beta");

        JsonNode page = getJson("/api/v1/public/players?team=" + teamA.get("slug").asText() + "&size=100", null);
        assertThat(page.get("content").findValuesAsText("slug")).contains(playerA.get("slug").asText());
        page.get("content").forEach(player -> assertThat(player.get("team").get("slug").asText())
                .isEqualTo(teamA.get("slug").asText()));

        JsonNode detail = getJson("/api/v1/public/players/" + playerA.get("slug").asText(), null);
        assertThat(detail.get("path").asText()).isEqualTo("/players/" + playerA.get("slug").asText());
        assertThat(detail.get("name").asText()).isEqualTo("Player Alpha");
        assertThat(detail.get("battingStyle").asText()).isEqualTo("Right-handed");
    }

    @Test
    void deactivatedPlayersAndTeamsDisappearFromThePublicApi() throws Exception {
        String token = adminToken();
        JsonNode team = createTeam(token, uniqueSlug("gone"), "Gone XI", true);
        JsonNode player = createPlayer(token, id(team), "Gone Player");

        mockMvc.perform(post("/api/v1/admin/players/" + player.get("id").asText() + "/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/admin/teams/" + team.get("id").asText() + "/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());

        performExpectingStatus(get("/api/v1/public/players/" + player.get("slug").asText()), 404);
        performExpectingStatus(get("/api/v1/public/teams/" + team.get("slug").asText()), 404);
    }

    @Test
    void unauthenticatedWritesAreRejected() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "Anonymous XI");
        payload.put("shortName", "ANO");
        performExpectingStatus(post("/api/v1/admin/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(payload)), 401);
        performExpectingStatus(get("/api/v1/admin/teams"), 401);
    }
}
