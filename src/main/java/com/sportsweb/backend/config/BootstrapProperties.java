package com.sportsweb.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Opt-in creation of the first administrator. Nothing is seeded by Flyway; credentials come
 * only from the environment and are never logged.
 */
@ConfigurationProperties(prefix = "app.bootstrap")
public class BootstrapProperties {

    private boolean enabled = false;
    private String adminEmail = "";
    private String adminPassword = "";
    private String adminDisplayName = "Site Administrator";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAdminDisplayName() {
        return adminDisplayName;
    }

    public void setAdminDisplayName(String adminDisplayName) {
        this.adminDisplayName = adminDisplayName;
    }
}
