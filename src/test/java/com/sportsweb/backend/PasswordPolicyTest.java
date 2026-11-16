package com.sportsweb.backend;

import com.sportsweb.backend.security.password.PasswordPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    @Test
    void acceptsAStrongPassword() {
        assertThat(PasswordPolicy.violations("Str0ng-Passw0rd!x")).isEmpty();
    }

    @Test
    void rejectsShortPasswords() {
        assertThat(PasswordPolicy.violations("Ab1!abc")).isNotEmpty();
    }

    @Test
    void rejectsPasswordsWithTooFewCharacterClasses() {
        assertThat(PasswordPolicy.violations("onlylowercaseletters")).isNotEmpty();
        assertThat(PasswordPolicy.violations("onlylowercaseletters1")).isNotEmpty();
    }

    @Test
    void rejectsWhitespace() {
        assertThat(PasswordPolicy.violations("Strong Password 1!")).isNotEmpty();
    }

    @Test
    void enforceProducesAClientFacingError() {
        assertThatThrownBy(() -> PasswordPolicy.enforce("weak"))
                .hasMessageContaining("at least");
    }
}
