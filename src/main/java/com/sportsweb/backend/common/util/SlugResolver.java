package com.sportsweb.backend.common.util;

import com.sportsweb.backend.common.error.ApiException;

import java.util.function.Predicate;

/**
 * Shared slug rules for content entities: a requested slug is normalised, validated, and made
 * unique on create; on update a clash with another row is a 409 instead of silently renaming.
 */
public final class SlugResolver {

    private static final int MAX_ATTEMPTS = 50;

    private SlugResolver() {
    }

    public static String forCreate(String requested, String fallbackSource, Predicate<String> exists) {
        String base = requested == null || requested.isBlank()
                ? Slugs.slugify(fallbackSource)
                : Slugs.slugify(requested);
        String candidate = validate(base);
        String resolved = candidate;
        int attempt = 1;
        while (exists.test(resolved)) {
            attempt++;
            if (attempt > MAX_ATTEMPTS) {
                throw ApiException.conflict("slug_taken", "Could not derive a unique slug from the given name.");
            }
            resolved = Slugs.withSuffix(candidate, attempt);
        }
        return resolved;
    }

    public static String forUpdate(String requested, String current, Predicate<String> exists) {
        if (requested == null || requested.isBlank()) {
            return current;
        }
        String candidate = validate(Slugs.slugify(requested));
        if (candidate.equals(current)) {
            return current;
        }
        if (exists.test(candidate)) {
            throw ApiException.conflict("slug_taken", "Another entry already uses this slug.");
        }
        return candidate;
    }

    private static String validate(String slug) {
        if (!Slugs.isValid(slug)) {
            throw ApiException.badRequest("invalid_slug",
                    "Slug must contain only lower case letters, digits and single hyphens.");
        }
        return slug;
    }
}
