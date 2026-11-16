package com.sportsweb.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Controls whether the OpenAPI document and Swagger UI are reachable anonymously. */
@ConfigurationProperties(prefix = "app.security.docs")
public class DocsProperties {

    private boolean publicAccess = true;

    public boolean isPublicAccess() {
        return publicAccess;
    }

    public void setPublicAccess(boolean publicAccess) {
        this.publicAccess = publicAccess;
    }
}
