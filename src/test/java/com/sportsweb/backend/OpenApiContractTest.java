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
    }

    @Test
    void swaggerUiIsServed() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
