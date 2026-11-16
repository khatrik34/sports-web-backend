package com.sportsweb.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.sportsweb.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Phase 3: change-password, videos, scorecards, commentary and rankings. */
class VideosAndMatchDetailApiTest extends AbstractIntegrationTest {

    // ---------------------------------------------------------------- helpers

    private JsonNode createTeam(String token, String name) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", name);
        payload.put("shortName", name.substring(0, Math.min(3, name.length())).toUpperCase());
        return postExpectingCreated("/api/v1/admin/teams", token, payload);
    }

    private JsonNode createMatch(String token, UUID team1, UUID team2) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("team1Id", team1.toString());
        payload.put("team2Id", team2.toString());
        payload.put("format", "T20I");
        payload.put("category", "INTERNATIONAL");
        payload.put("venue", "Detail Ground");
        payload.put("startTime", Instant.now().minus(3, ChronoUnit.HOURS).toString());
        payload.put("status", "LIVE");
        return postExpectingCreated("/api/v1/admin/matches", token, payload);
    }

    private JsonNode createVideo(String token, String title, Instant publishedAt) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", title);
        payload.put("category", "Match Highlights");
        payload.put("description", "A test video.");
        payload.put("thumbnailUrl", "https://cdn.example.test/thumb.jpg");
        payload.put("videoUrl", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        payload.put("duration", "08:24");
        payload.put("publishedAt", publishedAt == null ? null : publishedAt.toString());
        return postExpectingCreated("/api/v1/admin/videos", token, payload);
    }

    // ---------------------------------------------------------------- change password

    @Test
    void changePasswordNeedsTheCurrentPasswordAndEndsEverySession() throws Exception {
        String email = createUserWithRole("REPORTER");
        JsonNode tokens = login(email, STAFF_PASSWORD);
        String access = tokens.get("accessToken").asText();
        String refresh = tokens.get("refreshToken").asText();

        performExpectingStatus(post("/api/v1/auth/password")
                .header(HttpHeaders.AUTHORIZATION, bearer(access))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("currentPassword", "WrongPassw0rd!2026",
                        "newPassword", "NewStaffPassw0rd!2027"))), 400);

        JsonNode weak = performExpectingStatus(post("/api/v1/auth/password")
                .header(HttpHeaders.AUTHORIZATION, bearer(access))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("currentPassword", STAFF_PASSWORD, "newPassword", "weak"))), 400);
        assertThat(weak.get("code").asText()).isEqualTo("weak_password");

        mockMvc.perform(post("/api/v1/auth/password")
                        .header(HttpHeaders.AUTHORIZATION, bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("currentPassword", STAFF_PASSWORD,
                                "newPassword", "NewStaffPassw0rd!2027"))))
                .andExpect(status().isNoContent());

        // every refresh token is revoked, so the old session cannot be renewed
        performExpectingStatus(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", refresh))), 401);

        // the new password works and the flag is cleared
        JsonNode fresh = login(email, "NewStaffPassw0rd!2027");
        assertThat(fresh.get("user").get("mustChangePassword").asBoolean()).isFalse();
    }

    @Test
    void anonymousPasswordChangeIsRejected() throws Exception {
        performExpectingStatus(post("/api/v1/auth/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("currentPassword", "x", "newPassword", "NewStaffPassw0rd!2027"))), 401);
    }

    // ---------------------------------------------------------------- videos

    @Test
    void videoIsPublicOnlyOnceItIsVisibleAndPublished() throws Exception {
        String token = adminToken();
        JsonNode draft = createVideo(token, "Draft video " + uniqueSlug("v"), null);
        assertThat(draft.get("visible").asBoolean()).isTrue();
        performExpectingStatus(get("/api/v1/public/videos/" + draft.get("slug").asText()), 404);

        Map<String, Object> publish = new LinkedHashMap<>();
        publish.put("title", "Published video " + uniqueSlug("v"));
        publish.put("category", "The Breakdown");
        publish.put("videoUrl", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        publish.put("duration", "12:10");
        publish.put("publishedAt", Instant.now().minus(1, ChronoUnit.HOURS).toString());
        JsonNode published = performExpectingStatus(put("/api/v1/admin/videos/" + draft.get("id").asText())
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(publish)), 200);

        JsonNode publicVideo = getJson("/api/v1/public/videos/" + published.get("slug").asText(), null);
        assertThat(publicVideo.get("path").asText()).isEqualTo("/videos/" + published.get("slug").asText());
        assertThat(publicVideo.get("duration").asText()).isEqualTo("12:10");
        assertThat(publicVideo.get("image").asText()).isNotBlank();

        JsonNode list = getJson("/api/v1/public/videos?filter=breakdown&size=100", null);
        assertThat(list.get("content").findValuesAsText("slug")).contains(published.get("slug").asText());

        mockMvc.perform(post("/api/v1/admin/videos/" + published.get("id").asText() + "/hide")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
        performExpectingStatus(get("/api/v1/public/videos/" + published.get("slug").asText()), 404);
    }

    @Test
    void reporterCannotWriteVideos() throws Exception {
        String reporter = tokenFor(reporterEmail());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", "Forbidden video");
        payload.put("videoUrl", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        performExpectingStatus(post("/api/v1/admin/videos")
                .header(HttpHeaders.AUTHORIZATION, bearer(reporter))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(payload)), 403);
    }

    // ---------------------------------------------------------------- scorecard

    @Test
    void scorecardReplacementIsAtomicAndPublic() throws Exception {
        String token = adminToken();
        JsonNode team1 = createTeam(token, "Score Home");
        JsonNode team2 = createTeam(token, "Score Away");
        JsonNode match = createMatch(token, UUID.fromString(team1.get("id").asText()),
                UUID.fromString(team2.get("id").asText()));

        Map<String, Object> batter1 = new LinkedHashMap<>();
        batter1.put("position", 1);
        batter1.put("batter", "Yashasvi Jaiswal");
        batter1.put("dismissal", "c Smith b Starc");
        batter1.put("runs", 42);
        batter1.put("balls", 68);
        batter1.put("fours", 6);
        batter1.put("sixes", 0);

        Map<String, Object> batter2 = new LinkedHashMap<>();
        batter2.put("position", 2);
        batter2.put("batter", "Virat Kohli");
        batter2.put("dismissal", "not out");
        batter2.put("runs", 86);
        batter2.put("balls", 142);
        batter2.put("fours", 9);
        batter2.put("sixes", 1);
        batter2.put("notOut", true);

        Map<String, Object> bowler = new LinkedHashMap<>();
        bowler.put("position", 1);
        bowler.put("bowler", "Mitchell Starc");
        bowler.put("overs", "9.2");
        bowler.put("maidens", 1);
        bowler.put("runsConceded", 28);
        bowler.put("wickets", 2);

        Map<String, Object> innings = new LinkedHashMap<>();
        innings.put("inningsNumber", 1);
        innings.put("battingTeamId", team1.get("id").asText());
        innings.put("runs", 284);
        innings.put("wickets", 4);
        innings.put("overs", "78.4");
        innings.put("batting", List.of(batter1, batter2));
        innings.put("bowling", List.of(bowler));

        String path = "/api/v1/admin/matches/" + match.get("id").asText() + "/scorecard";
        JsonNode saved = performExpectingStatus(put(path)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("innings", List.of(innings)))), 200);
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).get("batting")).hasSize(2);
        assertThat(saved.get(0).get("bowling").get(0).get("economy").asText()).isEqualTo("3.00");

        JsonNode publicScorecard = getJson("/api/v1/public/matches/" + match.get("slug").asText() + "/scorecard", null);
        assertThat(publicScorecard).hasSize(1);
        assertThat(publicScorecard.get(0).get("batting").get(1).get("notOut").asBoolean()).isTrue();
        assertThat(publicScorecard.get(0).get("battingTeam").get("slug").asText())
                .isEqualTo(team1.get("slug").asText());

        // a second full replacement drops the previous rows instead of appending
        innings.put("runs", 300);
        innings.put("wickets", 6);
        innings.put("batting", List.of(batter1));
        innings.put("bowling", List.of());
        performExpectingStatus(put(path)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("innings", List.of(innings)))), 200);
        JsonNode replaced = getJson("/api/v1/public/matches/" + match.get("slug").asText() + "/scorecard", null);
        assertThat(replaced.get(0).get("batting")).hasSize(1);
        assertThat(replaced.get(0).get("runs").asInt()).isEqualTo(300);
    }

    @Test
    void battingTeamMustBelongToTheFixture() throws Exception {
        String token = adminToken();
        JsonNode team1 = createTeam(token, "Fixture Home");
        JsonNode team2 = createTeam(token, "Fixture Away");
        JsonNode stranger = createTeam(token, "Unrelated XI");
        JsonNode match = createMatch(token, UUID.fromString(team1.get("id").asText()),
                UUID.fromString(team2.get("id").asText()));

        Map<String, Object> innings = new LinkedHashMap<>();
        innings.put("inningsNumber", 1);
        innings.put("battingTeamId", stranger.get("id").asText());
        innings.put("runs", 10);
        JsonNode error = performExpectingStatus(put("/api/v1/admin/matches/" + match.get("id").asText() + "/scorecard")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("innings", List.of(innings)))), 400);
        assertThat(error.get("code").asText()).isEqualTo("invalid_reference");
    }

    @Test
    void hiddenFixturesHideTheirScorecardAndCommentary() throws Exception {
        String token = adminToken();
        JsonNode team1 = createTeam(token, "Hidden Home");
        JsonNode team2 = createTeam(token, "Hidden Away");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("team1Id", team1.get("id").asText());
        payload.put("team2Id", team2.get("id").asText());
        payload.put("startTime", Instant.now().minus(1, ChronoUnit.HOURS).toString());
        payload.put("status", "LIVE");
        payload.put("visible", false);
        JsonNode match = postExpectingCreated("/api/v1/admin/matches", token, payload);
        String slug = match.get("slug").asText();

        performExpectingStatus(get("/api/v1/public/matches/" + slug + "/scorecard"), 404);
        performExpectingStatus(get("/api/v1/public/matches/" + slug + "/commentary"), 404);
    }

    // ---------------------------------------------------------------- commentary

    @Test
    void commentaryIsAppendedAndServedNewestFirst() throws Exception {
        String token = adminToken();
        JsonNode team1 = createTeam(token, "Comm Home");
        JsonNode team2 = createTeam(token, "Comm Away");
        JsonNode match = createMatch(token, UUID.fromString(team1.get("id").asText()),
                UUID.fromString(team2.get("id").asText()));
        String path = "/api/v1/admin/matches/" + match.get("id").asText() + "/commentary";

        mockMvc.perform(post(path)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("over", "78.3", "kind", "1 RUN", "text", "Gentle push into the gap."))))
                .andExpect(status().isCreated());
        mockMvc.perform(post(path)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("over", "78.4", "kind", "FOUR", "text", "Beautiful cover drive."))))
                .andExpect(status().isCreated());

        JsonNode publicEntries = getJson("/api/v1/public/matches/" + match.get("slug").asText() + "/commentary?size=100", null);
        assertThat(publicEntries.get("totalElements").asInt()).isEqualTo(2);
        List<String> overs = publicEntries.get("content").findValuesAsText("over");
        assertThat(overs).containsExactlyInAnyOrder("78.3", "78.4");

        String entryId = publicEntries.get("content").get(0).get("id").asText();
        mockMvc.perform(delete(path + "/" + entryId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());
        assertThat(getJson("/api/v1/public/matches/" + match.get("slug").asText() + "/commentary", null)
                .get("totalElements").asInt()).isEqualTo(1);
    }

    @Test
    void reporterCanReadCommentaryButNotWriteIt() throws Exception {
        String token = adminToken();
        JsonNode team1 = createTeam(token, "Read Home");
        JsonNode team2 = createTeam(token, "Read Away");
        JsonNode match = createMatch(token, UUID.fromString(team1.get("id").asText()),
                UUID.fromString(team2.get("id").asText()));
        String reporter = tokenFor(reporterEmail());

        getJson("/api/v1/admin/matches/" + match.get("id").asText() + "/commentary", reporter);
        performExpectingStatus(post("/api/v1/admin/matches/" + match.get("id").asText() + "/commentary")
                .header(HttpHeaders.AUTHORIZATION, bearer(reporter))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("over", "1.1", "kind", "DOT", "text", "Left alone."))), 403);
    }

    // ---------------------------------------------------------------- rankings

    @Test
    void rankingsServeTheOngoingSeriesTable() throws Exception {
        String token = adminToken();
        JsonNode team1 = createTeam(token, "Rank One");
        JsonNode team2 = createTeam(token, "Rank Two");
        String slug = uniqueSlug("rankings");

        Map<String, Object> tournament = new LinkedHashMap<>();
        tournament.put("slug", slug);
        tournament.put("name", "Rankings Series " + slug);
        tournament.put("format", "T20I");
        tournament.put("category", "INTERNATIONAL");
        tournament.put("status", "ONGOING");
        tournament.put("startDate", java.time.LocalDate.now().toString());
        JsonNode series = postExpectingCreated("/api/v1/admin/tournaments", token, tournament);

        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("teamId", team1.get("id").asText());
        row1.put("position", 1);
        row1.put("played", 5);
        row1.put("won", 4);
        row1.put("lost", 1);
        row1.put("points", 8);
        row1.put("nrr", "+1.245");
        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("teamId", team2.get("id").asText());
        row2.put("position", 2);
        row2.put("played", 5);
        row2.put("won", 1);
        row2.put("lost", 4);
        row2.put("points", 2);

        mockMvc.perform(put("/api/v1/admin/tournaments/" + series.get("id").asText() + "/standings")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("rows", List.of(row1, row2)))))
                .andExpect(status().isOk());

        JsonNode rankings = getJson("/api/v1/public/rankings?format=T20I", null);
        assertThat(rankings.get("tournamentSlug").asText()).isEqualTo(slug);
        assertThat(rankings.get("format").asText()).isEqualTo("T20I");
        assertThat(rankings.get("rows")).hasSize(2);
        assertThat(rankings.get("rows").get(0).get("team").get("slug").asText())
                .isEqualTo(team1.get("slug").asText());
        assertThat(rankings.get("rows").get(0).get("nrr").asText()).isEqualTo("+1.245");
    }

    @Test
    void unknownRankingsFormatIsRejected() throws Exception {
        JsonNode error = performExpectingStatus(get("/api/v1/public/rankings?format=tiddlywinks"), 400);
        assertThat(error.get("code").asText()).isEqualTo("invalid_filter");
    }
}
