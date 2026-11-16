package com.sportsweb.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.sportsweb.backend.security.user.RoleCodes;
import com.sportsweb.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationApiTest extends AbstractIntegrationTest {

    @Test
    void bootstrapAdminCanSignInAndReceivesRolesAndPermissions() throws Exception {
        JsonNode tokens = login(ADMIN_EMAIL, ADMIN_PASSWORD);

        assertThat(tokens.get("accessToken").asText()).isNotBlank();
        assertThat(tokens.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(tokens.get("refreshToken").asText()).isNotBlank();
        assertThat(tokens.get("user").get("email").asText()).isEqualTo(ADMIN_EMAIL);
        assertThat(tokens.get("user").get("roles").toString()).contains(RoleCodes.SUPER_ADMIN);
        assertThat(tokens.get("user").get("permissions").toString()).contains("news:publish");
        assertThat(tokens.get("user").has("passwordHash")).isFalse();
    }

    @Test
    void wrongPasswordIsRejectedWithProblemDocument() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", ADMIN_EMAIL, "password", "definitely-not-the-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("invalid_credentials"));
    }

    @Test
    void unknownAccountIsRejectedWithoutRevealingExistence() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "nobody-" + uniqueSlug("x") + "@test.local", "password", "WhateverPassw0rd!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("invalid_credentials"));
    }

    @Test
    void accountLocksAfterFiveFailedAttempts() throws Exception {
        String email = createUserWithRole(RoleCodes.REPORTER);

        for (int attempt = 0; attempt < 5; attempt++) {
            expectLoginFailure(email, "wrong-password-" + attempt, 401);
        }

        // Even the correct password is refused while the lock is in place.
        expectLoginFailure(email, STAFF_PASSWORD, 403);
    }

    @Test
    void refreshRotatesTheTokenAndDetectsReuse() throws Exception {
        String refreshToken = login(ADMIN_EMAIL, ADMIN_PASSWORD).get("refreshToken").asText();

        JsonNode rotated = performExpectingStatus(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", refreshToken))), 200);
        String newRefreshToken = rotated.get("refreshToken").asText();
        assertThat(newRefreshToken).isNotEqualTo(refreshToken);

        // Presenting the old token again is treated as theft: rejected and all sessions revoked.
        performExpectingStatus(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", refreshToken))), 401);
        performExpectingStatus(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", newRefreshToken))), 401);
    }

    @Test
    void logoutRevokesTheRefreshToken() throws Exception {
        String refreshToken = login(ADMIN_EMAIL, ADMIN_PASSWORD).get("refreshToken").asText();

        performExpectingStatus(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", refreshToken))), 204);

        performExpectingStatus(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", refreshToken))), 401);
    }

    @Test
    void meRequiresAuthenticationAndReturnsTheProfile() throws Exception {
        performExpectingStatus(get("/api/v1/auth/me"), 401);

        String token = adminToken();
        mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(ADMIN_EMAIL))
                .andExpect(jsonPath("$.permissions").isArray());
    }

    @Test
    void adminCanCreateStaffUserWhoThenSignsIn() throws Exception {
        String email = uniqueEmail("editor");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", email);
        payload.put("displayName", "New Editor");
        payload.put("password", STAFF_PASSWORD);
        payload.put("roleCodes", java.util.List.of(RoleCodes.EDITOR));
        payload.put("mustChangePassword", true);

        mockMvc.perform(post("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.roles[0].code").value(RoleCodes.EDITOR))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        JsonNode tokens = login(email, STAFF_PASSWORD);
        assertThat(tokens.get("user").get("mustChangePassword").asBoolean()).isTrue();
    }

    @Test
    void weakPasswordIsRejectedOnUserCreation() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", uniqueEmail("weak"));
        payload.put("displayName", "Weak Password");
        payload.put("password", "short");
        payload.put("roleCodes", java.util.List.of(RoleCodes.REPORTER));

        mockMvc.perform(post("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("weak_password"));
    }
}
