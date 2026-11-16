package com.sportsweb.backend;

import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.config.LoginProperties;
import com.sportsweb.backend.security.auth.LoginRateLimitService;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Per-IP sign-in limiting, exercised directly so the shared Spring context stays unthrottled. */
class LoginRateLimitServiceTest {

    private LoginRateLimitService service(int maxAttempts, Duration window) {
        LoginProperties properties = new LoginProperties();
        properties.setIpMaxAttempts(maxAttempts);
        properties.setIpWindow(window);
        return new LoginRateLimitService(properties);
    }

    @Test
    void refusesFurtherAttemptsOnceTheWindowIsFull() {
        LoginRateLimitService service = service(3, Duration.ofMinutes(5));

        service.registerFailure("203.0.113.10");
        service.registerFailure("203.0.113.10");
        assertThatCode(() -> service.checkAllowed("203.0.113.10")).doesNotThrowAnyException();

        service.registerFailure("203.0.113.10");
        assertThatThrownBy(() -> service.checkAllowed("203.0.113.10"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("too_many_attempts"));
    }

    @Test
    void otherAddressesAndResetsAreUnaffected() {
        LoginRateLimitService service = service(1, Duration.ofMinutes(5));
        service.registerFailure("198.51.100.7");

        assertThatCode(() -> service.checkAllowed("198.51.100.8")).doesNotThrowAnyException();
        assertThatCode(() -> service.checkAllowed(null)).doesNotThrowAnyException();

        service.reset("198.51.100.7");
        assertThatCode(() -> service.checkAllowed("198.51.100.7")).doesNotThrowAnyException();
    }

    @Test
    void attemptsOlderThanTheWindowAreForgotten() {
        LoginRateLimitService service = service(1, Duration.ofMillis(50));
        service.registerFailure("192.0.2.44");
        try {
            Thread.sleep(120);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        assertThatCode(() -> service.checkAllowed("192.0.2.44")).doesNotThrowAnyException();
    }
}
