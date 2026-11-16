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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Phase 4: cross-section search and the forced password change gate. */
class Phase4ApiTest extends AbstractIntegrationTest {

    private static String token() {
        return "zq" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private JsonNode createTeam(String adminToken, String name) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", name);
        payload.put("shortName", name.substring(0, Math.min(3, name.length())).toUpperCase());
        return postExpectingCreated("/api/v1/admin/teams", adminToken, payload);
    }

    // ---------------------------------------------------------------- search

    @Test
    void searchSpansEverySectionAndHidesUnpublishedContent() throws Exception {
        String admin = adminToken();
        String term = token();

        JsonNode team = createTeam(admin, "Searchable XI " + term);
        JsonNode otherTeam = createTeam(admin, "Opponent XI " + term);

        Map<String, Object> player = new LinkedHashMap<>();
        player.put("fullName", "Searchable Player " + term);
        player.put("teamId", team.get("id").asText());
        player.put("role", "Batter");
        postExpectingCreated("/api/v1/admin/players", admin, player);

        Map<String, Object> series = new LinkedHashMap<>();
        series.put("name", "Searchable Series " + term);
        series.put("format", "T20I");
        series.put("status", "ONGOING");
        postExpectingCreated("/api/v1/admin/tournaments", admin, series);

        Map<String, Object> video = new LinkedHashMap<>();
        video.put("title", "Searchable Video " + term);
        video.put("videoUrl", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        video.put("publishedAt", Instant.now().minus(1, ChronoUnit.HOURS).toString());
        postExpectingCreated("/api/v1/admin/videos", admin, video);

        Map<String, Object> fixture = new LinkedHashMap<>();
        fixture.put("team1Id", team.get("id").asText());
        fixture.put("team2Id", otherTeam.get("id").asText());
        fixture.put("startTime", Instant.now().minus(2, ChronoUnit.HOURS).toString());
        fixture.put("status", "COMPLETED");
        postExpectingCreated("/api/v1/admin/matches", admin, fixture);

        // one published and one draft article both contain the term
        JsonNode draft = createArticle(admin, "Draft story " + term, "Draft summary", null);
        JsonNode published = createArticle(admin, "Published story " + term, "Published summary", null);
        publishArticle(admin, published.get("id").asText(), null);

        JsonNode response = getJson("/api/v1/public/search?q=" + term + "&limit=5", null);
        assertThat(response.get("query").asText()).isEqualTo(term);
        List<String> types = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        response.get("results").forEach(hit -> {
            types.add(hit.get("type").asText());
            paths.add(hit.get("path").asText());
        });

        assertThat(types).contains("news", "team", "player", "series", "match", "video");
        assertThat(paths).contains("/news/" + published.get("slug").asText());
        assertThat(paths).doesNotContain("/news/" + draft.get("slug").asText());
        assertThat(response.get("total").asInt()).isEqualTo(response.get("results").size());

        // every hit carries a usable absolute URL
        response.get("results").forEach(hit ->
                assertThat(hit.get("url").asText()).startsWith("https://www.example.test/"));
    }

    @Test
    void searchRejectsQueriesThatAreTooShort() throws Exception {
        JsonNode error = performExpectingStatus(get("/api/v1/public/search?q=a"), 400);
        assertThat(error.get("code").asText()).isEqualTo("query_too_short");
    }

    // ---------------------------------------------------------------- forced password change

    @Test
    void forcedPasswordChangeBlocksEverythingExceptTheAuthEndpoints() throws Exception {
        String admin = adminToken();
        String email = uniqueEmail("forced");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", email);
        payload.put("displayName", "Forced Change");
        payload.put("password", STAFF_PASSWORD);
        payload.put("roleCodes", List.of("EDITOR"));
        payload.put("mustChangePassword", true);
        mockMvc.perform(post("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isCreated());

        JsonNode tokens = login(email, STAFF_PASSWORD);
        assertThat(tokens.get("user").get("mustChangePassword").asBoolean()).isTrue();
        String accessToken = tokens.get("accessToken").asText();

        // business endpoints are refused with a distinct code
        JsonNode blocked = performExpectingStatus(get("/api/v1/admin/news")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)), 403);
        assertThat(blocked.get("code").asText()).isEqualTo("password_change_required");

        // the account can still read itself and change its password
        JsonNode me = getJson("/api/v1/auth/me", accessToken);
        assertThat(me.get("mustChangePassword").asBoolean()).isTrue();

        mockMvc.perform(post("/api/v1/auth/password")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("currentPassword", STAFF_PASSWORD,
                                "newPassword", "FreshPassw0rd!2027"))))
                .andExpect(status().isNoContent());

        // a token issued after the change is unrestricted
        JsonNode fresh = login(email, "FreshPassw0rd!2027");
        assertThat(fresh.get("user").get("mustChangePassword").asBoolean()).isFalse();
        getJson("/api/v1/admin/news", fresh.get("accessToken").asText());
    }
}
