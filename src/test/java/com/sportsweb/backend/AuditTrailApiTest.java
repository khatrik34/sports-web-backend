package com.sportsweb.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.sportsweb.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditTrailApiTest extends AbstractIntegrationTest {

    @Test
    void failedSignInForUnknownAccountIsAudited() throws Exception {
        String attemptedEmail = uniqueEmail("ghost");
        expectLoginFailure(attemptedEmail, "Inc0rrectPassw0rd!", 401);

        JsonNode page = getJson(
                "/api/v1/admin/audit-logs?action=AUTH_LOGIN_FAILED&entityType=auth&size=50", adminToken());

        boolean found = false;
        for (JsonNode entry : page.get("content")) {
            if (attemptedEmail.equals(entry.path("entityId").asText())
                    && "FAILURE".equals(entry.path("outcome").asText())) {
                found = true;
            }
        }
        assertThat(found).as("failed sign-in is recorded in the audit trail").isTrue();
    }

    @Test
    void publishingIsAuditedWithActorAndSnapshot() throws Exception {
        String adminToken = adminToken();
        JsonNode article = createArticle(adminToken, "Audited story " + uniqueSlug("t"), "Public summary", null);
        String articleId = article.get("id").asText();
        publishArticle(adminToken, articleId, null);

        JsonNode page = getJson(
                "/api/v1/admin/audit-logs?entityType=news_article&entityId=" + articleId + "&size=50", adminToken());

        boolean published = false;
        for (JsonNode entry : page.get("content")) {
            if ("NEWS_PUBLISHED".equals(entry.path("action").asText())) {
                published = true;
                assertThat(entry.path("actorEmail").asText()).isEqualTo(ADMIN_EMAIL);
                assertThat(entry.path("after").path("status").asText()).isEqualTo("PUBLISHED");
            }
        }
        assertThat(published).as("NEWS_PUBLISHED audit entry exists").isTrue();
    }

    @Test
    void auditTrailRequiresPermission() throws Exception {
        mockMvc.perform(post("/api/v1/admin/news")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
