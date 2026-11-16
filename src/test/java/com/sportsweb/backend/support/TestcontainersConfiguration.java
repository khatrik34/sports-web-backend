package com.sportsweb.backend.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Real PostgreSQL for tests: the schema, Flyway migrations and PostgreSQL specific SQL
 * (jsonb, timestamptz, partial indexes) are exercised exactly as in production.
 * Requires Docker on the machine running the build (GitHub Actions ubuntu-latest has it).
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @ServiceConnection
    @Bean
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("sportsweb")
                .withUsername("sportsweb")
                .withPassword("sportsweb");
    }
}
