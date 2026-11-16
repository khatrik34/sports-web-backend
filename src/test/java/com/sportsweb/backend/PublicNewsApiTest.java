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
                .andExpect(jsonPath("$.seo.canonicalUrl").value("https://www.example.test/" + categorySlug + "/" + articleSlug))
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

    private boolean listContainsSlug(JsonNode page, String slug) {
        for (JsonNode item : page.get("content")) {
            if (slug.equals(item.get("slug").asText())) {
                return true;
            }
        }
        return false;
    }
}
