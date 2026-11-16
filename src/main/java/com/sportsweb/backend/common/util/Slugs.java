package com.sportsweb.backend.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** URL slug helper: lower case, ASCII, hyphen separated, stable enough to keep old URLs. */
public final class Slugs {

    private static final Pattern NON_ASCII = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_HYPHENS = Pattern.compile("(^-+)|(-+$)");
    private static final Pattern VALID = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    private Slugs() {
    }

    public static String slugify(String input) {
        if (input == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
        String ascii = decomposed.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        String slug = EDGE_HYPHENS.matcher(NON_ASCII.matcher(ascii).replaceAll("-")).replaceAll("");
        return slug.length() > 180 ? truncate(slug, 180) : slug;
    }

    public static boolean isValid(String slug) {
        return slug != null && slug.length() <= 200 && VALID.matcher(slug).matches();
    }

    /** Appends -2, -3 ... until the slug is free. Used for generated slugs only. */
    public static String withSuffix(String slug, int attempt) {
        return attempt <= 1 ? slug : slug + "-" + attempt;
    }

    private static String truncate(String slug, int maxLength) {
        String cut = slug.substring(0, maxLength);
        int lastHyphen = cut.lastIndexOf('-');
        if (lastHyphen > 20) {
            cut = cut.substring(0, lastHyphen);
        }
        return EDGE_HYPHENS.matcher(cut).replaceAll("");
    }
}
