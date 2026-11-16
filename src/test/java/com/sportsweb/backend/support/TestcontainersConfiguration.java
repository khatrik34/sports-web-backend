package com.sportsweb.backend.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Real PostgreSQL for tests: the schema, Flyway migrations and PostgreSQL specific SQL
 * (jsonb, timestamptz, partial indexes) are exercised exactly as in production.
 * Requires Docker on the machine running the build. Environments without Docker (some CI
 * runners) can run the same suite against an already-running PostgreSQL by passing
 * {@code -Dtest.database.external=true} and setting SPRING_DATASOURCE_URL/USERNAME/PASSWORD.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @ServiceConnection
    @Bean
    @ConditionalOnProperty(name = "test.database.external", havingValue = "false", matchIfMissing = true)
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("sportsweb")
                .withUsername("sportsweb")
                .withPassword("sportsweb");
    }
}
