package com.sportsweb.backend.security.auth;

import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.config.LoginProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP limit on failed sign-ins, on top of the per-account lockout. It is an in-memory sliding
 * window: correct for a single instance, and deliberately simple. A multi-instance deployment must
 * move this counter to Redis (recorded as a limitation in docs/PROGRESS.md).
 */
@Service
public class LoginRateLimitService {

    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration window;

    public LoginRateLimitService(LoginProperties properties) {
        this.maxAttempts = Math.max(properties.getIpMaxAttempts(), 1);
        this.window = properties.getIpWindow() == null ? Duration.ofMinutes(10) : properties.getIpWindow();
    }

    /** Called before checking credentials so a blocked client never reaches the password hash. */
    public void checkAllowed(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return;
        }
        Deque<Instant> attempts = failures.get(ipAddress);
        if (attempts == null) {
            return;
        }
        Instant cutoff = Instant.now().minus(window);
        synchronized (attempts) {
            prune(attempts, cutoff);
            if (attempts.size() >= maxAttempts) {
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "too_many_attempts",
                        "Too many failed sign-in attempts from this address. Try again later.");
            }
        }
    }

    public void registerFailure(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return;
        }
        Deque<Instant> attempts = failures.computeIfAbsent(ipAddress, key -> new ArrayDeque<>());
        Instant cutoff = Instant.now().minus(window);
        synchronized (attempts) {
            prune(attempts, cutoff);
            attempts.addLast(Instant.now());
        }
    }

    public void reset(String ipAddress) {
        if (ipAddress != null) {
            failures.remove(ipAddress);
        }
    }

    private void prune(Deque<Instant> attempts, Instant cutoff) {
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) {
            attempts.removeFirst();
        }
    }
}
