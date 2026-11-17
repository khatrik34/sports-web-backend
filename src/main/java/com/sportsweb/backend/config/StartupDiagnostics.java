package com.sportsweb.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * One line at startup that answers the questions people actually ask when a fresh install looks empty:
 * is the demo seed on the Flyway path, which database am I connected to, and is the first administrator
 * still being bootstrapped. Without this, "the tables exist but there is no data" is silent.
 */
@Component
public class StartupDiagnostics implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupDiagnostics.class);

    private final String flywayLocations;
    private final String datasourceUrl;
    private final boolean bootstrapEnabled;

    public StartupDiagnostics(@Value("${spring.flyway.locations:classpath:db/migration}") String flywayLocations,
                              @Value("${spring.datasource.url:}") String datasourceUrl,
                              @Value("${app.bootstrap.enabled:false}") boolean bootstrapEnabled) {
        this.flywayLocations = flywayLocations;
        this.datasourceUrl = datasourceUrl;
        this.bootstrapEnabled = bootstrapEnabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Database: {}", maskCredentials(datasourceUrl));
        log.info("Flyway locations: {}", flywayLocations);
        if (flywayLocations != null && flywayLocations.contains("db/demo")) {
            log.info("Demo seed: ENABLED (db/demo is on the Flyway path).");
        } else {
            log.warn("Demo seed: DISABLED - no sample data will be loaded. To load it, run with "
                    + "--spring.profiles.active=demo or set "
                    + "SPRING_FLYWAY_LOCATIONS=classpath:db/migration,classpath:db/demo");
        }
        log.info("Bootstrap administrator: {}", bootstrapEnabled ? "enabled" : "disabled");
    }

    /** Never print credentials that a JDBC URL may carry (jdbc:postgresql://user:secret@host/db). */
    private String maskCredentials(String url) {
        if (url == null || url.isBlank()) {
            return "(unset)";
        }
        return url.replaceAll("//[^/@]*@", "//");
    }
}
