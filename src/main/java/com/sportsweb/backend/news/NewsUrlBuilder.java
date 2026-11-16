package com.sportsweb.backend.news;

import com.sportsweb.backend.config.PublicSiteProperties;
import org.springframework.stereotype.Component;

/**
 * Builds public paths and absolute URLs. The path shape is configuration, because the real
 * URLs of the existing website still have to be confirmed; a stored canonical path always
 * wins so previously published URLs never change.
 */
@Component
public class NewsUrlBuilder {

    private final PublicSiteProperties properties;

    public NewsUrlBuilder(PublicSiteProperties properties) {
        this.properties = properties;
    }

    /** Path used for API payloads: stored canonical path first, otherwise derived. */
    public String path(NewsArticle article) {
        String canonical = article.getCanonicalPath();
        if (canonical != null && !canonical.isBlank()) {
            return canonical;
        }
        return derivePath(article);
    }

    /** Path derived purely from the template, ignoring any stored canonical path. */
    public String derivePath(NewsArticle article) {
        String template = properties.getArticlePathTemplate() == null ? "/{categorySlug}/{slug}"
                : properties.getArticlePathTemplate();
        String categorySlug = article.getCategory() == null ? null : article.getCategory().getSlug();
        String path = template;
        if (categorySlug == null || categorySlug.isBlank()) {
            path = path.replace("/{categorySlug}", "");
        } else {
            path = path.replace("{categorySlug}", categorySlug);
        }
        path = path.replace("{slug}", article.getSlug());
        return path.startsWith("/") ? path : "/" + path;
    }

    public String categoryPath(NewsCategory category) {
        return "/category/" + category.getSlug();
    }

    public String absoluteUrl(String path) {
        String base = properties.getBaseUrl() == null ? "" : properties.getBaseUrl().trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + (path.startsWith("/") ? path : "/" + path);
    }
}
