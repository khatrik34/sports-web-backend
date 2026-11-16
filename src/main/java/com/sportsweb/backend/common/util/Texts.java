package com.sportsweb.backend.common.util;

/** Small text helpers shared by the admin services. */
public final class Texts {

    private Texts() {
    }

    /** Trims and converts blank strings to {@code null} so the database stores absence, not "". */
    public static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
