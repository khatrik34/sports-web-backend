package com.sportsweb.backend;

import com.sportsweb.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
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
        String script = new String(new ClassPathResource("db/demo/R__demo_data.sql")
                .getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(script);
                }

                assertThat(count(connection, "select count(*) from cricket_team where id::text like '11111111%'"))
                        .isEqualTo(4);
                assertThat(count(connection, "select count(*) from cricket_player where id::text like '22222222%'"))
                        .isEqualTo(12);
                assertThat(count(connection, "select count(*) from cricket_match where status = 'LIVE'"))
                        .isGreaterThanOrEqualTo(1);
                assertThat(count(connection, "select count(*) from cricket_match where status = 'COMPLETED'"))
                        .isGreaterThanOrEqualTo(1);
                assertThat(count(connection, "select count(*) from cricket_match where status = 'SCHEDULED'"))
                        .isGreaterThanOrEqualTo(1);
                assertThat(count(connection, "select count(*) from cricket_innings"))
                        .isGreaterThanOrEqualTo(2);
                assertThat(count(connection, "select count(*) from cricket_batting_entry"))
                        .isGreaterThanOrEqualTo(10);
                assertThat(count(connection, "select count(*) from cricket_bowling_entry"))
                        .isGreaterThanOrEqualTo(8);
                assertThat(count(connection, "select count(*) from cricket_commentary"))
                        .isGreaterThanOrEqualTo(6);
                assertThat(count(connection, "select count(*) from cricket_standing"))
                        .isGreaterThanOrEqualTo(4);
                assertThat(count(connection, "select count(*) from news_article where status = 'PUBLISHED'"))
                        .isGreaterThanOrEqualTo(5);
                assertThat(count(connection, "select count(*) from news_category where id::text like '99999999%'"))
                        .isEqualTo(2);
                assertThat(count(connection, "select count(*) from video where id::text like 'bbbbbbbb%'"))
                        .isEqualTo(3);
                assertThat(count(connection, "select count(*) from homepage_section"))
                        .isGreaterThanOrEqualTo(5);
                assertThat(count(connection, "select count(*) from seo_redirect where from_path = '/cricket/live'"))
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

    private long count(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }
}
