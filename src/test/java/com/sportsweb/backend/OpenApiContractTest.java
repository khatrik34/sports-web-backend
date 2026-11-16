package com.sportsweb.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.sportsweb.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The OpenAPI document is the contract the website and the admin panel build against, so its
 * generation is asserted rather than assumed.
 */
class OpenApiContractTest extends AbstractIntegrationTest {

    @Test
    void openApiDocumentExposesBothAudiencesAndTheBearerScheme() throws Exception {
        JsonNode document = getJson("/v3/api-docs", null);

        assertThat(document.path("info").path("version").asText()).isEqualTo("v1");
        assertThat(document.path("components").path("securitySchemes").has("bearerAuth")).isTrue();

        JsonNode paths = document.path("paths");
        assertThat(paths.has("/api/v1/auth/login")).isTrue();
        assertThat(paths.has("/api/v1/public/news")).isTrue();
        assertThat(paths.has("/api/v1/public/news/{slug}")).isTrue();
        assertThat(paths.has("/api/v1/admin/news")).isTrue();
        assertThat(paths.has("/api/v1/admin/users")).isTrue();
        assertThat(paths.has("/api/v1/admin/audit-logs")).isTrue();
        // Phase 2: cricket domain
        assertThat(paths.has("/api/v1/public/teams")).isTrue();
        assertThat(paths.has("/api/v1/public/players/{slug}")).isTrue();
        assertThat(paths.has("/api/v1/public/tournaments/{slug}")).isTrue();
        assertThat(paths.has("/api/v1/public/matches")).isTrue();
        assertThat(paths.has("/api/v1/admin/teams")).isTrue();
        assertThat(paths.has("/api/v1/admin/tournaments/{id}/standings")).isTrue();
        assertThat(paths.has("/api/v1/admin/matches/{id}")).isTrue();
        // Phase 3
        assertThat(paths.has("/api/v1/auth/password")).isTrue();
        assertThat(paths.has("/api/v1/public/videos")).isTrue();
        assertThat(paths.has("/api/v1/public/videos/{slug}")).isTrue();
        assertThat(paths.has("/api/v1/admin/videos")).isTrue();
        assertThat(paths.has("/api/v1/public/matches/{slug}/scorecard")).isTrue();
        assertThat(paths.has("/api/v1/public/matches/{slug}/commentary")).isTrue();
        assertThat(paths.has("/api/v1/admin/matches/{id}/scorecard")).isTrue();
        assertThat(paths.has("/api/v1/public/rankings")).isTrue();
        assertThat(paths.has("/api/v1/public/redirects")).isTrue();
        assertThat(paths.has("/api/v1/admin/redirects")).isTrue();
        assertThat(paths.has("/api/v1/public/homepage")).isTrue();
        assertThat(paths.has("/api/v1/admin/homepage")).isTrue();
        assertThat(paths.has("/api/v1/admin/media")).isTrue();
        // Phase 4
        assertThat(paths.has("/api/v1/public/search")).isTrue();
    }

    /**
     * Publishes the contract as a build artifact (CI uploads target/openapi.json) so the website and
     * the admin panel can generate types from exactly what this build serves.
     */
    @Test
    void openApiDocumentIsSnapshottedForTheClients() throws Exception {
        JsonNode document = getJson("/v3/api-docs", null);
        java.nio.file.Path target = java.nio.file.Path.of("target");
        java.nio.file.Files.createDirectories(target);
        java.nio.file.Files.writeString(target.resolve("openapi.json"),
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(document));
        assertThat(java.nio.file.Files.size(target.resolve("openapi.json"))).isGreaterThan(1000L);
    }

    @Test
    void swaggerUiIsServed() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
