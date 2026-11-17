package com.sportsweb.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.sportsweb.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sitemap/robots behaviour and the public fixture filter contract.
 *
 * <p>Confirmed bug fixed here: both sitemap feeds used to include articles whose {@code robots}
 * contains {@code noindex}, i.e. the site advertised pages it asked crawlers to ignore. Those articles
 * stay publicly readable — only the feeds exclude them.</p>
 */
class PublicSitemapAndFixtureContractTest extends AbstractIntegrationTest {

    private JsonNode createArticleWith(String adminToken, String title, String robots, String canonicalPath)
            throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>(articlePayload(title, "Summary for " + title, null));
        if (robots != null) {
            payload.put("robots", robots);
        }
        if (canonicalPath != null) {
            payload.put("canonicalPath", canonicalPath);
        }
        return postExpectingCreated("/api/v1/admin/news", adminToken, payload);
    }

    private List<String> sitemapUrls() throws Exception {
        JsonNode body = getJson("/api/v1/public/sitemap", null);
        List<String> urls = new ArrayList<>();
        body.forEach(entry -> urls.add(entry.get("loc").asText()));
        return urls;
    }

    private List<String> newsSitemapUrls() throws Exception {
        JsonNode body = getJson("/api/v1/public/news-sitemap", null);
        List<String> urls = new ArrayList<>();
        body.forEach(entry -> urls.add(entry.get("loc").asText()));
        return urls;
    }

    // ---------------------------------------------------------------- noindex

    @Test
    void noindexArticlesAreExcludedFromBothFeedsButStayReachable() throws Exception {
        String admin = adminToken();

        JsonNode indexable = createArticleWith(admin, "Indexable story " + uniqueSlug("idx"), null, null);
        publishArticle(admin, indexable.get("id").asText(), null);

        JsonNode noindex = createArticleWith(admin, "Hidden story " + uniqueSlug("noidx"),
                "noindex,nofollow", null);
        publishArticle(admin, noindex.get("id").asText(), null);

        String indexablePath = "/news/" + indexable.get("slug").asText();
        String noindexPath = "/news/" + noindex.get("slug").asText();

        assertThat(sitemapUrls()).contains("https://www.example.test" + indexablePath);
        assertThat(sitemapUrls()).doesNotContain("https://www.example.test" + noindexPath);
        assertThat(newsSitemapUrls()).contains("https://www.example.test" + indexablePath);
        assertThat(newsSitemapUrls()).doesNotContain("https://www.example.test" + noindexPath);

        // noindex is an SEO instruction, not an access rule: the page must still render.
        JsonNode detail = getJson("/api/v1/public/news/" + noindex.get("slug").asText(), null);
        assertThat(detail.get("seo").get("robots").asText()).contains("noindex");
        assertThat(detail.get("title").asText()).isEqualTo(noindex.get("title").asText());
    }

    // ---------------------------------------------------------------- drafts / archived / future

    @Test
    void draftsArchivedAndFutureArticlesNeverAppearInTheFeeds() throws Exception {
        String admin = adminToken();

        JsonNode draft = createArticleWith(admin, "Draft story " + uniqueSlug("draft"), null, null);

        JsonNode archived = createArticleWith(admin, "Archived story " + uniqueSlug("arch"), null, null);
        publishArticle(admin, archived.get("id").asText(), null);
        mockMvc.perform(post("/api/v1/admin/news/" + archived.get("id").asText() + "/archive")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk());

        JsonNode future = createArticleWith(admin, "Future story " + uniqueSlug("future"), null, null);
        publishArticle(admin, future.get("id").asText(),
                Instant.now().plus(2, ChronoUnit.DAYS).toString());

        List<String> sitemap = sitemapUrls();
        List<String> newsSitemap = newsSitemapUrls();
        for (JsonNode article : List.of(draft, archived, future)) {
            String path = "https://www.example.test/news/" + article.get("slug").asText();
            assertThat(sitemap).doesNotContain(path);
            assertThat(newsSitemap).doesNotContain(path);
            performExpectingStatus(get("/api/v1/public/news/" + article.get("slug").asText()), 404);
        }
    }

    // ---------------------------------------------------------------- custom canonical path

    @Test
    void customCanonicalPathIsUsedInThePayloadAndInTheSitemap() throws Exception {
        String admin = adminToken();
        String canonical = "/cricket/series/asia-cup-2026/story-" + uniqueSlug("canon");

        JsonNode article = createArticleWith(admin, "Canonical story " + uniqueSlug("canon"), null, canonical);
        publishArticle(admin, article.get("id").asText(), null);

        JsonNode detail = getJson("/api/v1/public/news/" + article.get("slug").asText(), null);
        assertThat(detail.get("path").asText()).isEqualTo(canonical);

        // The feed advertises the canonical URL, so the website must serve it or 301 to it
        // (documented in docs/CONTRACTS.md — the API never rewrites stored canonical paths).
        assertThat(sitemapUrls()).contains("https://www.example.test" + canonical);
        assertThat(newsSitemapUrls()).contains("https://www.example.test" + canonical);
    }

    // ---------------------------------------------------------------- fixture filters

    @Test
    void fixturesSupportTodayDateRangeAndPhaseFilters() throws Exception {
        String admin = adminToken();
        JsonNode teamA = postExpectingCreated("/api/v1/admin/teams", admin, Map.of(
                "name", "Range A " + uniqueSlug("ra"), "shortName", "RGA"));
        JsonNode teamB = postExpectingCreated("/api/v1/admin/teams", admin, Map.of(
                "name", "Range B " + uniqueSlug("rb"), "shortName", "RGB"));

        String today = LocalDate.now(ZoneOffset.UTC).toString();
        String inSixDays = LocalDate.now(ZoneOffset.UTC).plusDays(6).toString();

        Map<String, Object> soon = new LinkedHashMap<>();
        soon.put("team1Id", teamA.get("id").asText());
        soon.put("team2Id", teamB.get("id").asText());
        soon.put("startTime", Instant.now().plus(2, ChronoUnit.HOURS).toString());
        soon.put("status", "SCHEDULED");
        JsonNode soonMatch = postExpectingCreated("/api/v1/admin/matches", admin, soon);

        Map<String, Object> later = new LinkedHashMap<>();
        later.put("team1Id", teamB.get("id").asText());
        later.put("team2Id", teamA.get("id").asText());
        later.put("startTime", Instant.now().plus(6, ChronoUnit.DAYS).toString());
        later.put("status", "SCHEDULED");
        JsonNode laterMatch = postExpectingCreated("/api/v1/admin/matches", admin, later);

        // from/to are UTC days, both inclusive
        JsonNode sameDay = getJson("/api/v1/public/matches?from=" + today + "&to=" + today, null);
        List<String> sameDaySlugs = slugs(sameDay);
        assertThat(sameDaySlugs).contains(soonMatch.get("slug").asText());
        assertThat(sameDaySlugs).doesNotContain(laterMatch.get("slug").asText());

        JsonNode onlyLater = getJson("/api/v1/public/matches?from=" + inSixDays, null);
        assertThat(slugs(onlyLater)).contains(laterMatch.get("slug").asText());
        assertThat(slugs(onlyLater)).doesNotContain(soonMatch.get("slug").asText());

        // the pre-existing single-date filter and the phase filter keep working
        assertThat(slugs(getJson("/api/v1/public/matches?date=" + today, null)))
                .contains(soonMatch.get("slug").asText());
        getJson("/api/v1/public/matches?filter=live", null);
        getJson("/api/v1/public/matches?filter=upcoming&size=5", null);

        // invalid combinations are rejected, not silently ignored
        JsonNode badRange = performExpectingStatus(
                get("/api/v1/public/matches?from=" + inSixDays + "&to=" + today), 400);
        assertThat(badRange.get("code").asText()).isEqualTo("invalid_date_range");

        JsonNode mixed = performExpectingStatus(
                get("/api/v1/public/matches?date=" + today + "&from=" + today), 400);
        assertThat(mixed.get("code").asText()).isEqualTo("invalid_date_range");

        JsonNode badDate = performExpectingStatus(get("/api/v1/public/matches?from=16-11-2026"), 400);
        assertThat(badDate.get("code").asText()).isEqualTo("invalid_date");
    }

    private List<String> slugs(JsonNode page) {
        List<String> slugs = new ArrayList<>();
        page.get("content").forEach(match -> slugs.add(match.get("slug").asText()));
        return slugs;
    }
}
