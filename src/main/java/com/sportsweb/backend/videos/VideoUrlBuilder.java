package com.sportsweb.backend.videos;

import com.sportsweb.backend.config.PublicSiteProperties;
import org.springframework.stereotype.Component;

/** Videos live at /videos/{slug} on the shipped website. */
@Component
public class VideoUrlBuilder {

    private final String baseUrl;

    public VideoUrlBuilder(PublicSiteProperties publicSiteProperties) {
        String base = publicSiteProperties.getBaseUrl() == null ? "" : publicSiteProperties.getBaseUrl().trim();
        this.baseUrl = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    public String path(String slug) {
        return "/videos/" + slug;
    }

    public String absolute(String path) {
        return baseUrl + path;
    }
}
