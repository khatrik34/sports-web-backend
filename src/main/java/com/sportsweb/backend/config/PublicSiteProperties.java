package com.sportsweb.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Describes the public website so API payloads can carry the same URLs and canonical links
 * the site already uses. Both values are environment driven because the real URL shape of
 * the existing site still has to be confirmed (see docs/AUDIT.md).
 */
@ConfigurationProperties(prefix = "app.public-site")
public class PublicSiteProperties {

    private String baseUrl = "http://localhost:3000";
    /** Confirmed against the live site: articles are served at /news/{slug}. */
    private String articlePathTemplate = "/news/{slug}";
    /** Value of the {@code lang} attribute used by the news sitemap feed. */
    private String language = "en";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getArticlePathTemplate() {
        return articlePathTemplate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setArticlePathTemplate(String articlePathTemplate) {
        this.articlePathTemplate = articlePathTemplate;
    }
}
