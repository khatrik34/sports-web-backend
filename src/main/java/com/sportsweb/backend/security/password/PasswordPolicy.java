package com.sportsweb.backend.security.password;

import com.sportsweb.backend.common.error.ApiException;

import java.util.ArrayList;
import java.util.List;

/**
 * Server side password policy. Applied on user creation, password reset and (later) change.
 * Deliberately boring and deterministic so it can be unit tested.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 12;
    public static final int MAX_LENGTH = 128;

    private PasswordPolicy() {
    }

    public static List<String> violations(String password) {
        List<String> violations = new ArrayList<>();
        if (password == null || password.length() < MIN_LENGTH) {
            violations.add("Password must be at least " + MIN_LENGTH + " characters long.");
            return violations;
        }
        if (password.length() > MAX_LENGTH) {
            violations.add("Password must be at most " + MAX_LENGTH + " characters long.");
        }
        int classes = 0;
        if (password.chars().anyMatch(Character::isLowerCase)) {
            classes++;
        }
        if (password.chars().anyMatch(Character::isUpperCase)) {
            classes++;
        }
        if (password.chars().anyMatch(Character::isDigit)) {
            classes++;
        }
        if (password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch))) {
            classes++;
        }
        if (classes < 3) {
            violations.add("Password must combine at least three of: lower case, upper case, digit, symbol.");
        }
        if (password.chars().anyMatch(Character::isWhitespace)) {
            violations.add("Password must not contain whitespace.");
        }
        return violations;
    }

    public static void enforce(String password) {
        List<String> violations = violations(password);
        if (!violations.isEmpty()) {
            throw ApiException.badRequest("weak_password", String.join(" ", violations));
        }
    }
}
