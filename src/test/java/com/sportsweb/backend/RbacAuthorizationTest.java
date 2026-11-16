package com.sportsweb.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.sportsweb.backend.security.user.RoleCodes;
import com.sportsweb.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Permissions are enforced by the server, not by whichever UI calls it. */
class RbacAuthorizationTest extends AbstractIntegrationTest {

    @Test
    void anonymousRequestsToTheAdminApiAreUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/news"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthenticated"));
    }

    @Test
    void anonymousRequestsToTheUserAdminApiAreUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reporterCanWriteDraftsButCannotPublish() throws Exception {
        String reporterToken = tokenFor(reporterEmail());

        JsonNode article = createArticle(reporterToken, "Reporter draft " + uniqueSlug("t"), "Summary", null);
        String articleId = article.get("id").asText();

        mockMvc.perform(post("/api/v1/admin/news/" + articleId + "/publish")
                        .header(HttpHeaders.AUTHORIZATION, bearer(reporterToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("forbidden"));
    }

    @Test
    void reporterCannotManageUsers() throws Exception {
        String reporterToken = tokenFor(reporterEmail());

        mockMvc.perform(get("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(reporterToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .header(HttpHeaders.AUTHORIZATION, bearer(reporterToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void auditorCanReadAuditTrailButCannotWriteContent() throws Exception {
        String auditorToken = tokenFor(createUserWithRole(RoleCodes.AUDITOR));

        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auditorToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/admin/news")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auditorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(articlePayload("Auditor attempt " + uniqueSlug("t"), "Summary", null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void editorCanPublish() throws Exception {
        String editorToken = tokenFor(editorEmail());
        JsonNode article = createArticle(editorToken, "Editor article " + uniqueSlug("t"), "Summary", null);

        postExpectingStatus(post("/api/v1/admin/news/" + article.get("id").asText() + "/publish")
                .header(HttpHeaders.AUTHORIZATION, bearer(editorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of())), 200);
    }

    private void postExpectingStatus(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder,
                                     int status) throws Exception {
        mockMvc.perform(builder).andExpect(status().is(status));
    }
}
