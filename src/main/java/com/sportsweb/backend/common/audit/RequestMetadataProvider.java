package com.sportsweb.backend.common.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Reads the client IP and user agent of the current request.
 *
 * <p>{@code X-Forwarded-For} is only trusted because the application is expected to run
 * behind a reverse proxy that overwrites the header (see README deployment notes).</p>
 */
@Component
public class RequestMetadataProvider {

    private static final int MAX_USER_AGENT = 512;

    public RequestMetadata current() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return RequestMetadata.EMPTY;
        }
        HttpServletRequest request = attributes.getRequest();
        return new RequestMetadata(truncate(clientIp(request), 64), truncate(request.getHeader("User-Agent"), MAX_USER_AGENT));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
