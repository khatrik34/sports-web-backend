package com.sportsweb.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.sportsweb.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Phase 3: SEO redirects, homepage configuration and media metadata. */
class SeoHomepageMediaApiTest extends AbstractIntegrationTest {

    private JsonNode createRedirect(String token, String from, String to, int statusCode, boolean enabled)
            throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("fromPath", from);
        payload.put("toPath", to);
        payload.put("statusCode", statusCode);
        payload.put("note", "Moved during migration");
        payload.put("enabled", enabled);
        return postExpectingCreated("/api/v1/admin/redirects", token, payload);
    }

    // ---------------------------------------------------------------- redirects

    @Test
    void redirectsResolvePubliclyAndCanBeRetiredOrDisabled() throws Exception {
        String token = adminToken();
        String from = "/legacy-" + uniqueSlug("story");
        createRedirect(token, from + "/", "/news/current-story", 301, true);

        JsonNode resolved = getJson("/api/v1/public/redirects?path=" + from, null);
        assertThat(resolved.get("toPath").asText()).isEqualTo("/news/current-story");
        assertThat(resolved.get("statusCode").asInt()).isEqualTo(301);

        // 410 retires a URL without a target
        String retired = "/retired-" + uniqueSlug("page");
        createRedirect(token, retired, null, 410, true);
        JsonNode gone = getJson("/api/v1/public/redirects?path=" + retired, null);
        assertThat(gone.get("statusCode").asInt()).isEqualTo(410);
        assertThat(gone.get("toPath").isNull()).isTrue();

        // unknown paths and disabled redirects both 404
        performExpectingStatus(get("/api/v1/public/redirects?path=/never-existed-" + uniqueSlug("x")), 404);
        String disabled = "/disabled-" + uniqueSlug("page");
        JsonNode created = createRedirect(token, disabled, "/news/somewhere", 302, false);
        performExpectingStatus(get("/api/v1/public/redirects?path=" + disabled), 404);

        Map<String, Object> enable = new LinkedHashMap<>();
        enable.put("fromPath", disabled);
        enable.put("toPath", "/news/somewhere");
        enable.put("statusCode", 302);
        enable.put("enabled", true);
        mockMvc.perform(put("/api/v1/admin/redirects/" + created.get("id").asText())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(enable)))
                .andExpect(status().isOk());
        assertThat(getJson("/api/v1/public/redirects?path=" + disabled, null).get("statusCode").asInt())
                .isEqualTo(302);
    }

    @Test
    void redirectValidationRejectsBadStatusDuplicatesAndQueryStrings() throws Exception {
        String token = adminToken();
        Map<String, Object> badStatus = new LinkedHashMap<>();
        badStatus.put("fromPath", "/bad-status-" + uniqueSlug("p"));
        badStatus.put("toPath", "/news/x");
        badStatus.put("statusCode", 200);
        JsonNode invalid = performExpectingStatus(post("/api/v1/admin/redirects")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(badStatus)), 400);
        assertThat(invalid.get("code").asText()).isEqualTo("invalid_status");

        String from = "/dup-" + uniqueSlug("p");
        createRedirect(token, from, "/news/one", 301, true);
        Map<String, Object> duplicate = new LinkedHashMap<>();
        duplicate.put("fromPath", from);
        duplicate.put("toPath", "/news/two");
        JsonNode conflict = performExpectingStatus(post("/api/v1/admin/redirects")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(duplicate)), 409);
        assertThat(conflict.get("code").asText()).isEqualTo("redirect_exists");

        Map<String, Object> withQuery = new LinkedHashMap<>();
        withQuery.put("fromPath", "/old?utm_source=x");
        withQuery.put("toPath", "/news/new");
        JsonNode pathError = performExpectingStatus(post("/api/v1/admin/redirects")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(withQuery)), 400);
        assertThat(pathError.get("code").asText()).isEqualTo("invalid_path");
    }

    // ---------------------------------------------------------------- homepage

    @Test
    void homepageConfigurationIsOrderedAndHidesDisabledBlocks() throws Exception {
        String token = adminToken();

        Map<String, Object> newsRail = new LinkedHashMap<>();
        newsRail.put("key", "news-rail");
        newsRail.put("type", "headline_list");
        newsRail.put("title", "Latest stories");
        newsRail.put("sortOrder", 2);
        newsRail.put("config", Map.of("articleSlugs", List.of("a", "b"), "limit", 3));

        Map<String, Object> hero = new LinkedHashMap<>();
        hero.put("key", "hero");
        hero.put("type", "hero");
        hero.put("title", "Lead story");
        hero.put("sortOrder", 1);
        hero.put("config", Map.of("articleSlug", "a"));

        Map<String, Object> hidden = new LinkedHashMap<>();
        hidden.put("key", "promo");
        hidden.put("type", "custom");
        hidden.put("sortOrder", 3);
        hidden.put("enabled", false);
        hidden.put("config", Map.of("html", "<p>hidden</p>"));

        mockMvc.perform(put("/api/v1/admin/homepage")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("sections", List.of(newsRail, hero, hidden)))))
                .andExpect(status().isOk());

        JsonNode publicBlocks = getJson("/api/v1/public/homepage", null);
        assertThat(publicBlocks).hasSize(2);
        assertThat(publicBlocks.get(0).get("key").asText()).isEqualTo("hero");
        assertThat(publicBlocks.get(1).get("key").asText()).isEqualTo("news-rail");
        assertThat(publicBlocks.get(1).get("type").asText()).isEqualTo("HEADLINE_LIST");
        assertThat(publicBlocks.get(1).get("config").get("articleSlugs").isArray()).isTrue();

        // the admin view still sees the disabled block
        assertThat(getJson("/api/v1/admin/homepage", token)).hasSize(3);

        // duplicate keys are rejected atomically
        Map<String, Object> duplicateHero = new LinkedHashMap<>(hero);
        JsonNode duplicate = performExpectingStatus(put("/api/v1/admin/homepage")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("sections", List.of(hero, duplicateHero)))), 400);
        assertThat(duplicate.get("code").asText()).isEqualTo("duplicate_section");
    }

    // ---------------------------------------------------------------- media

    @Test
    void mediaAssetsCanBeRegisteredListedAndRemoved() throws Exception {
        String token = adminToken();
        String url = "https://cdn.example.test/" + uniqueSlug("image") + ".jpg";

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("url", url);
        payload.put("filename", "hero.jpg");
        payload.put("contentType", "image/jpeg");
        payload.put("width", 1600);
        payload.put("height", 900);
        payload.put("altText", "Cricket stadium at dusk");
        payload.put("credit", "The Boundary");
        JsonNode created = postExpectingCreated("/api/v1/admin/media", token, payload);
        assertThat(created.get("altText").asText()).isEqualTo("Cricket stadium at dusk");

        JsonNode list = getJson("/api/v1/admin/media?size=100", token);
        assertThat(list.get("content").findValuesAsText("url")).contains(url);

        Map<String, Object> invalid = new LinkedHashMap<>();
        invalid.put("url", "not-a-url");
        JsonNode error = performExpectingStatus(post("/api/v1/admin/media")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(invalid)), 400);
        assertThat(error.get("code").asText()).isEqualTo("invalid_url");

        mockMvc.perform(delete("/api/v1/admin/media/" + created.get("id").asText())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());
    }

    // ---------------------------------------------------------------- permissions

    @Test
    void auditorCanReadOperationsButNotChangeThem() throws Exception {
        String auditor = tokenFor(createUserWithRole("AUDITOR"));
        getJson("/api/v1/admin/redirects", auditor);
        getJson("/api/v1/admin/homepage", auditor);
        getJson("/api/v1/admin/media", auditor);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("url", "https://cdn.example.test/forbidden.jpg");
        performExpectingStatus(post("/api/v1/admin/media")
                .header(HttpHeaders.AUTHORIZATION, bearer(auditor))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(payload)), 403);

        Map<String, Object> redirect = new LinkedHashMap<>();
        redirect.put("fromPath", "/forbidden-" + uniqueSlug("p"));
        redirect.put("toPath", "/news/x");
        performExpectingStatus(post("/api/v1/admin/redirects")
                .header(HttpHeaders.AUTHORIZATION, bearer(auditor))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(redirect)), 403);
    }

    @Test
    void operationsEndpointsRequireAuthentication() throws Exception {
        performExpectingStatus(get("/api/v1/admin/redirects"), 401);
        performExpectingStatus(get("/api/v1/admin/homepage"), 401);
        performExpectingStatus(get("/api/v1/admin/media"), 401);
        // the public homepage feed stays anonymous
        getJson("/api/v1/public/homepage", null);
    }
}
