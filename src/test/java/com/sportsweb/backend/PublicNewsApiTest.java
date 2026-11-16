package com.sportsweb.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.sportsweb.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Draft leakage is the highest risk in this project: cover it explicitly. */
class PublicNewsApiTest extends AbstractIntegrationTest {

    @Test
    void draftIsInvisibleToThePublicApi() throws Exception {
        String adminToken = adminToken();
        String slug = uniqueSlug("draft");
        JsonNode article = createArticle(adminToken, "Draft only " + slug, "Internal summary", null);
        assertThat(article.get("status").asText()).isEqualTo("DRAFT");

        String articleSlug = article.get("slug").asText();

        mockMvc.perform(get("/api/v1/public/news/" + articleSlug))
                .andExpect(status().isNotFound());

        JsonNode page = getJson("/api/v1/public/news?size=100", null);
        assertThat(listContainsSlug(page, articleSlug)).isFalse();
    }

    @Test
    void publishedArticleIsPublicWithSeoMetadataAndNoAdminFields() throws Exception {
        String adminToken = adminToken();
        String categorySlug = uniqueSlug("cat");
        createCategory(adminToken, categorySlug);

        JsonNode article = createArticle(adminToken, "Published story " + uniqueSlug("t"), "Public summary", categorySlug);
        String articleId = article.get("id").asText();
        String articleSlug = article.get("slug").asText();
        publishArticle(adminToken, articleId, null);

        mockMvc.perform(get("/api/v1/public/news/" + articleSlug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value(articleSlug))
                .andExpect(jsonPath("$.categorySlug").value(categorySlug))
                .andExpect(jsonPath("$.seo.canonicalUrl").value("https://www.example.test/news/" + articleSlug))
                .andExpect(jsonPath("$.path").value("/news/" + articleSlug))
                .andExpect(jsonPath("$.readTime").isNumber())
                .andExpect(jsonPath("$.seo.robots").value("index,follow"))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(jsonPath("$.version").doesNotExist())
                .andExpect(jsonPath("$.authorUser").doesNotExist());

        JsonNode detail = getJson("/api/v1/public/news/" + articleSlug, null);
        List<String> fieldNames = new ArrayList<>();
        detail.fieldNames().forEachRemaining(fieldNames::add);
        assertThat(fieldNames).contains("slug", "title", "body", "seo", "path", "url");
        assertThat(fieldNames).doesNotContain("id", "status", "version", "archivedAt");
    }

    @Test
    void futureDatedArticleBehavesLikeAScheduledArticle() throws Exception {
        String adminToken = adminToken();
        JsonNode article = createArticle(adminToken, "Scheduled story " + uniqueSlug("t"), "Public summary", null);
        String articleId = article.get("id").asText();
        String articleSlug = article.get("slug").asText();

        String tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toString();
        JsonNode published = publishArticle(adminToken, articleId, tomorrow);
        assertThat(published.get("status").asText()).isEqualTo("PUBLISHED");

        mockMvc.perform(get("/api/v1/public/news/" + articleSlug))
                .andExpect(status().isNotFound());

        JsonNode page = getJson("/api/v1/public/news?size=100", null);
        assertThat(listContainsSlug(page, articleSlug)).isFalse();
    }

    @Test
    void archivedArticleIsInvisibleToThePublicApi() throws Exception {
        String adminToken = adminToken();
        JsonNode article = createArticle(adminToken, "Archived story " + uniqueSlug("t"), "Public summary", null);
        String articleId = article.get("id").asText();
        String articleSlug = article.get("slug").asText();
        publishArticle(adminToken, articleId, null);

        mockMvc.perform(get("/api/v1/public/news/" + articleSlug)).andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/admin/news/" + articleId + "/archive")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/public/news/" + articleSlug))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownSlugReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/public/news/does-not-exist-" + uniqueSlug("x")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void publicEndpointsAreReachableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/public/news?size=5")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/public/categories")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/public/sitemap")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/public/news/latest?limit=5")).andExpect(status().isOk());
    }

    @Test
    void sitemapUsesTheRealNewsUrlShape() throws Exception {
        String adminToken = adminToken();
        JsonNode article = createArticle(adminToken, "Sitemap story " + uniqueSlug("t"), "Public summary", null);
        String articleSlug = article.get("slug").asText();
        publishArticle(adminToken, article.get("id").asText(), null);

        JsonNode entries = getJson("/api/v1/public/sitemap", null);
        boolean found = false;
        for (JsonNode entry : entries) {
            if (("https://www.example.test/news/" + articleSlug).equals(entry.path("loc").asText())) {
                found = true;
                assertThat(entry.path("lastModified").isTextual()).isTrue();
            }
        }
        assertThat(found).as("published article is listed as /news/{slug}").isTrue();
    }

    @Test
    void newsSitemapOnlyContainsRecentlyPublishedArticles() throws Exception {
        String adminToken = adminToken();
        JsonNode fresh = createArticle(adminToken, "Fresh story " + uniqueSlug("t"), "Public summary", null);
        JsonNode old = createArticle(adminToken, "Old story " + uniqueSlug("t"), "Public summary", null);
        publishArticle(adminToken, fresh.get("id").asText(), null);
        String tenDaysAgo = java.time.Instant.now().minus(10, ChronoUnit.DAYS).toString();
        publishArticle(adminToken, old.get("id").asText(), tenDaysAgo);

        JsonNode feed = getJson("/api/v1/public/news-sitemap", null);
        boolean hasFresh = false;
        boolean hasOld = false;
        for (JsonNode entry : feed) {
            String loc = entry.path("loc").asText();
            if (loc.endsWith("/news/" + fresh.get("slug").asText())) {
                hasFresh = true;
                assertThat(entry.path("title").asText()).isNotBlank();
                assertThat(entry.path("language").asText()).isEqualTo("en");
            }
            if (loc.endsWith("/news/" + old.get("slug").asText())) {
                hasOld = true;
            }
        }
        assertThat(hasFresh).as("article published now is in the 48h news sitemap").isTrue();
        assertThat(hasOld).as("article published ten days ago is outside the 48h window").isFalse();

        // the window is bounded server side
        mockMvc.perform(get("/api/v1/public/news-sitemap?hours=999999")).andExpect(status().isOk());
    }

    @Test
    void categoryPathMatchesTheWebsiteFilterUrl() throws Exception {
        String adminToken = adminToken();
        String slug = uniqueSlug("cat");
        createCategory(adminToken, slug);

        JsonNode categories = getJson("/api/v1/public/categories", null);
        boolean found = false;
        for (JsonNode category : categories) {
            if (slug.equals(category.path("slug").asText())) {
                found = true;
                assertThat(category.path("path").asText()).isEqualTo("/news?filter=" + slug);
                assertThat(category.path("url").asText()).isEqualTo("https://www.example.test/news?filter=" + slug);
            }
        }
        assertThat(found).as("category is listed with the website filter URL").isTrue();
    }

    private boolean listContainsSlug(JsonNode page, String slug) {
        for (JsonNode item : page.get("content")) {
            if (slug.equals(item.get("slug").asText())) {
                return true;
            }
        }
        return false;
    }
}
