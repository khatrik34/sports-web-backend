package com.sportsweb.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.sportsweb.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NewsAdminApiTest extends AbstractIntegrationTest {

    @Test
    void creatingTwoArticlesWithTheSameSlugConflicts() throws Exception {
        String adminToken = adminToken();
        String slug = uniqueSlug("same-slug");

        Map<String, Object> first = articlePayload("First " + slug, "Summary", null);
        first.put("slug", slug);
        postExpectingCreated("/api/v1/admin/news", adminToken, first);

        Map<String, Object> second = articlePayload("Second " + slug, "Summary", null);
        second.put("slug", slug);

        mockMvc.perform(post("/api/v1/admin/news")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(second)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("slug_taken"));
    }

    @Test
    void publishingWithoutSummaryIsRejected() throws Exception {
        String adminToken = adminToken();
        JsonNode article = createArticle(adminToken, "No summary " + uniqueSlug("t"), null, null);

        mockMvc.perform(post("/api/v1/admin/news/" + article.get("id").asText() + "/publish")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("not_publishable"));
    }

    @Test
    void unpublishingRemovesTheArticleFromThePublicApi() throws Exception {
        String adminToken = adminToken();
        JsonNode article = createArticle(adminToken, "Unpublish me " + uniqueSlug("t"), "Summary", null);
        String articleId = article.get("id").asText();
        String articleSlug = article.get("slug").asText();
        publishArticle(adminToken, articleId, null);

        mockMvc.perform(get("/api/v1/public/news/" + articleSlug)).andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/admin/news/" + articleId + "/unpublish")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        mockMvc.perform(get("/api/v1/public/news/" + articleSlug)).andExpect(status().isNotFound());
    }

    @Test
    void archivedArticlesCannotBePublishedAgain() throws Exception {
        String adminToken = adminToken();
        JsonNode article = createArticle(adminToken, "Archive me " + uniqueSlug("t"), "Summary", null);
        String articleId = article.get("id").asText();

        mockMvc.perform(post("/api/v1/admin/news/" + articleId + "/archive")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        mockMvc.perform(post("/api/v1/admin/news/" + articleId + "/publish")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("invalid_transition"));
    }

    @Test
    void updateReplacesEditableFields() throws Exception {
        String adminToken = adminToken();
        JsonNode article = createArticle(adminToken, "Original title " + uniqueSlug("t"), "Original summary", null);
        String articleId = article.get("id").asText();

        Map<String, Object> update = new LinkedHashMap<>();
        update.put("title", "Updated title");
        update.put("summary", "Updated summary");
        update.put("body", "<p>Updated body</p>");
        update.put("featured", false);

        mockMvc.perform(put("/api/v1/admin/news/" + articleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.summary").value("Updated summary"))
                .andExpect(jsonPath("$.featured").value(false));
    }

    @Test
    void unknownCategoryIsRejected() throws Exception {
        String adminToken = adminToken();
        Map<String, Object> payload = articlePayload("Category " + uniqueSlug("t"), "Summary", "no-such-category");

        mockMvc.perform(post("/api/v1/admin/news")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("unknown_category"));
    }

    @Test
    void adminListIncludesDraftsAndIsGuarded() throws Exception {
        String adminToken = adminToken();
        createArticle(adminToken, "Draft visible only to staff " + uniqueSlug("t"), "Summary", null);

        mockMvc.perform(get("/api/v1/admin/news?status=DRAFT&size=100")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(get("/api/v1/admin/news")).andExpect(status().isUnauthorized());
    }
}
