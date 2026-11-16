package com.sportsweb.backend.seo.dto;

/** Answer to "what happened to this path?" for the website's 404 handler. */
public record PublicRedirect(String fromPath, String toPath, int statusCode) {
}
