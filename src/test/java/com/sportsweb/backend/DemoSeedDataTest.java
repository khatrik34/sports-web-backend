package com.sportsweb.backend;

import com.sportsweb.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The demo dataset lives in db/demo and is opt-in. This test executes it inside a transaction that is
 * always rolled back, so the SQL is proven against the real schema (columns, checks, foreign keys)
 * without leaking rows into the other test classes.
 */
class DemoSeedDataTest extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void demoScriptAppliesToTheRealSchemaAndLeavesNothingBehind() throws Exception {
        // Every db/demo file, in the same alphabetical order Flyway applies them.
        Resource[] scripts = new PathMatchingResourcePatternResolver().getResources("classpath*:db/demo/*.sql");
        Arrays.sort(scripts, Comparator.comparing(Resource::getFilename));
        StringBuilder combined = new StringBuilder();
        for (Resource resource : scripts) {
            combined.append(new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8))
                    .append('\n');
        }
        String script = combined.toString();

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(script);
                }

                assertThat(count(connection, "select count(*) from cricket_team where id::text like '11111111%'"))
                        .isEqualTo(8);
                assertThat(count(connection, "select count(*) from cricket_player where id::text like '22222222%'"))
                        .isEqualTo(24);
                assertThat(count(connection, "select count(*) from cricket_match where status = 'LIVE'"))
                        .isGreaterThanOrEqualTo(1);
                assertThat(count(connection, "select count(*) from cricket_match where status = 'COMPLETED'"))
                        .isGreaterThanOrEqualTo(1);
                assertThat(count(connection, "select count(*) from cricket_match where status = 'SCHEDULED'"))
                        .isGreaterThanOrEqualTo(1);
                assertThat(count(connection, "select count(*) from cricket_innings"))
                        .isGreaterThanOrEqualTo(4);
                assertThat(count(connection, "select count(*) from cricket_batting_entry"))
                        .isGreaterThanOrEqualTo(18);
                assertThat(count(connection, "select count(*) from cricket_bowling_entry"))
                        .isGreaterThanOrEqualTo(14);
                assertThat(count(connection, "select count(*) from cricket_commentary"))
                        .isGreaterThanOrEqualTo(12);
                assertThat(count(connection, "select count(*) from cricket_standing"))
                        .isGreaterThanOrEqualTo(7);
                assertThat(count(connection, "select count(*) from news_article where status = 'PUBLISHED'"))
                        .isGreaterThanOrEqualTo(12);
                assertThat(count(connection, "select count(*) from news_category where id::text like '99999999%'"))
                        .isEqualTo(2);
                assertThat(count(connection, "select count(*) from video where id::text like 'bbbbbbbb%'"))
                        .isEqualTo(6);
                assertThat(count(connection, "select count(*) from homepage_section"))
                        .isGreaterThanOrEqualTo(6);
                assertThat(count(connection, "select count(*) from seo_redirect"))
                        .isGreaterThanOrEqualTo(3);
                // exactly one seeded article asks not to be indexed
                assertThat(count(connection, "select count(*) from news_article where robots like '%noindex%'"))
                        .isEqualTo(1);

                // the live fixture must be linkable through the public contract
                assertThat(count(connection, "select count(*) from cricket_match "
                        + "where slug = 'pakistan-vs-india-asia-cup-2026' and visible"))
                        .isEqualTo(1);
            } finally {
                connection.rollback();
            }
        }

        try (Connection check = dataSource.getConnection()) {
            assertThat(count(check, "select count(*) from cricket_team "
                    + "where id = '11111111-1111-1111-1111-111111111101'::uuid"))
                    .as("rolled back seed must not persist")
                    .isZero();
        }
    }

    /**
     * scripts/demo-data.sql is the single file people paste into pgAdmin. It is generated from the two
     * Flyway sources, so this guard fails if someone edits a source and forgets to rebuild it.
     */
    @Test
    void theSinglePasteScriptStaysInSyncWithTheSeedSources() throws Exception {
        String combined = Files.readString(Path.of("scripts", "demo-data.sql"));
        Resource[] scripts = new PathMatchingResourcePatternResolver().getResources("classpath*:db/demo/*.sql");
        assertThat(scripts).isNotEmpty();
        for (Resource resource : scripts) {
            String source = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertThat(combined)
                    .as("scripts/demo-data.sql must contain all of %s", resource.getFilename())
                    .contains(source.trim());
        }
    }

    private long count(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }
}
