package com.sportsweb.backend.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsweb.backend.security.user.RoleCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for API tests. Boots the whole application against a PostgreSQL container and
 * drives the real HTTP layer through MockMvc, including Spring Security.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    protected static final String ADMIN_EMAIL = "admin@test.local";
    protected static final String ADMIN_PASSWORD = "TestAdminPassw0rd!2026";
    protected static final String STAFF_PASSWORD = "StaffPassw0rd!2026";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // ---------------------------------------------------------------- authentication

    protected JsonNode login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return read(result);
    }

    protected String accessToken(String email, String password) throws Exception {
        return login(email, password).get("accessToken").asText();
    }

    protected String adminToken() throws Exception {
        return accessToken(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    protected void expectLoginFailure(String email, String password, int expectedStatus) throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().is(expectedStatus));
    }

    // ---------------------------------------------------------------- staff helpers

    protected String createUserWithRole(String roleCode) throws Exception {
        return createUserWithRole(roleCode, uniqueEmail(roleCode.toLowerCase()));
    }

    protected String createUserWithRole(String roleCode, String email) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", email);
        payload.put("displayName", "Test " + roleCode);
        payload.put("password", STAFF_PASSWORD);
        payload.put("roleCodes", List.of(roleCode));
        payload.put("mustChangePassword", false);
        mockMvc.perform(post("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isCreated());
        return email;
    }

    protected String reporterEmail() throws Exception {
        return createUserWithRole(RoleCodes.REPORTER);
    }

    protected String editorEmail() throws Exception {
        return createUserWithRole(RoleCodes.EDITOR);
    }

    protected String tokenFor(String email) throws Exception {
        return accessToken(email, STAFF_PASSWORD);
    }

    // ---------------------------------------------------------------- content helpers

    protected JsonNode createCategory(String token, String slug) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("slug", slug);
        payload.put("name", "Category " + slug);
        payload.put("description", "Test category");
        payload.put("sortOrder", 0);
        return postExpectingCreated("/api/v1/admin/news-categories", token, payload);
    }

    protected JsonNode createArticle(String token, String title, String summary, String categorySlug) throws Exception {
        return postExpectingCreated("/api/v1/admin/news", token, articlePayload(title, summary, categorySlug));
    }

    protected Map<String, Object> articlePayload(String title, String summary, String categorySlug) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", title);
        payload.put("subtitle", "Test subtitle");
        payload.put("summary", summary);
        payload.put("body", "<p>Body of " + title + "</p>");
        payload.put("bodyFormat", "HTML");
        payload.put("categorySlug", categorySlug);
        payload.put("coverImageUrl", "https://cdn.example.test/cover.jpg");
        payload.put("metaTitle", null);
        payload.put("metaDescription", null);
        payload.put("featured", Boolean.TRUE);
        return payload;
    }

    protected JsonNode publishArticle(String token, String articleId, String publishedAtIso) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("publishedAt", publishedAtIso);
        MvcResult result = mockMvc.perform(post("/api/v1/admin/news/" + articleId + "/publish")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isOk())
                .andReturn();
        return read(result);
    }

    // ---------------------------------------------------------------- generic helpers

    protected JsonNode getJson(String path, String token) throws Exception {
        MvcResult result = mockMvc.perform(authenticated(get(path), token))
                .andExpect(status().isOk())
                .andReturn();
        return read(result);
    }

    protected JsonNode postExpectingCreated(String path, String token, Object body) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return read(result);
    }

    protected org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticated(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder, String token) {
        return token == null ? builder : builder.header(HttpHeaders.AUTHORIZATION, bearer(token));
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected JsonNode read(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return body == null || body.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
    }

    protected JsonNode performExpectingStatus(RequestBuilder request, int expectedStatus) throws Exception {
        MvcResult result = mockMvc.perform(request).andExpect(status().is(expectedStatus)).andReturn();
        return read(result);
    }

    protected static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8) + "@test.local";
    }

    protected static String uniqueSlug(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
