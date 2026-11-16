package com.sportsweb.backend.cricket;

import com.sportsweb.backend.config.PublicSiteProperties;
import org.springframework.stereotype.Component;

/**
 * URLs of the shipped website (verified from sports-web routes): teams live at /teams/{slug},
 * players at /players/{slug}, series at /cricket/series/{slug} and matches at
 * /cricket/match/{slug}. Static paths are not duplicated here.
 */
@Component
public class CricketUrlBuilder {

    private final String baseUrl;

    public CricketUrlBuilder(PublicSiteProperties publicSiteProperties) {
        String base = publicSiteProperties.getBaseUrl() == null ? "" : publicSiteProperties.getBaseUrl().trim();
        this.baseUrl = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    public String teamPath(String slug) {
        return "/teams/" + slug;
    }

    public String playerPath(String slug) {
        return "/players/" + slug;
    }

    public String tournamentPath(String slug) {
        return "/cricket/series/" + slug;
    }

    public String matchPath(String slug) {
        return "/cricket/match/" + slug;
    }

    public String absolute(String path) {
        return baseUrl + path;
    }
}
