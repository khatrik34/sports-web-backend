package com.sportsweb.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Brute force protection for the credential endpoint. */
@ConfigurationProperties(prefix = "app.security.login")
public class LoginProperties {

    private int maxFailedAttempts = 5;
    private Duration lockoutDuration = Duration.ofMinutes(15);

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
}
