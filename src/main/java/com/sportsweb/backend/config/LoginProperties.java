package com.sportsweb.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Brute force protection for the credential endpoint. */
@ConfigurationProperties(prefix = "app.security.login")
public class LoginProperties {

    private int maxFailedAttempts = 5;
    private Duration lockoutDuration = Duration.ofMinutes(15);
    /** Failed sign-ins allowed per client IP inside {@link #ipWindow} before requests are refused. */
    private int ipMaxAttempts = 30;
    private Duration ipWindow = Duration.ofMinutes(10);

    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public void setMaxFailedAttempts(int maxFailedAttempts) {
        this.maxFailedAttempts = maxFailedAttempts;
    }

    public Duration getLockoutDuration() {
        return lockoutDuration;
    }

    public void setLockoutDuration(Duration lockoutDuration) {
        this.lockoutDuration = lockoutDuration;
    }

    public int getIpMaxAttempts() {
        return ipMaxAttempts;
    }

    public void setIpMaxAttempts(int ipMaxAttempts) {
        this.ipMaxAttempts = ipMaxAttempts;
    }

    public Duration getIpWindow() {
        return ipWindow;
    }

    public void setIpWindow(Duration ipWindow) {
        this.ipWindow = ipWindow;
    }
}
