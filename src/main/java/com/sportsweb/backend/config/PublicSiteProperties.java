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
    private String articlePathTemplate = "/{categorySlug}/{slug}";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getArticlePathTemplate() {
        return articlePathTemplate;
    }

    public void setArticlePathTemplate(String articlePathTemplate) {
        this.articlePathTemplate = articlePathTemplate;
    }
}
