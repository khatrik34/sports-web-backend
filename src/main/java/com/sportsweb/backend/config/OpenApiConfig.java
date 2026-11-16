package com.sportsweb.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI sportsWebOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sports Web API")
                        .version("v1")
                        .description("""
                                Backend for the public sports website and the admin panel.

                                * `/api/v1/public/**` — anonymous, published content only.
                                * `/api/v1/auth/**` — sign in, refresh, logout, current profile.
                                * `/api/v1/admin/**` — staff endpoints guarded by permissions.
                                """)
                        .contact(new Contact().name("Sports Web"))
                        .license(new License().name("Proprietary")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Access token from POST /api/v1/auth/login")));
    }
}
