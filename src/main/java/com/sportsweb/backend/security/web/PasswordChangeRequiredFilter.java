package com.sportsweb.backend.security.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Refuses business calls while the signed-in account still has {@code mustChangePassword} set.
 *
 * <p>The flag travels in the access token, so the check costs no database round trip. Only the auth
 * endpoints are allowed through: {@code /me} to render the screen, {@code /password} to fix it,
 * {@code /refresh} and {@code /logout} to end the session. A fresh token (issued after the change)
 * no longer carries the claim.</p>
 */
@Component
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    public PasswordChangeRequiredFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication
                && Boolean.TRUE.equals(jwtAuthentication.getToken().getClaimAsBoolean("must_change_password"))
                && !isAllowed(request.getRequestURI())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/problem+json");
            response.setCharacterEncoding("UTF-8");
            Map<String, Object> problem = new LinkedHashMap<>();
            problem.put("type", "about:blank");
            problem.put("title", "Forbidden");
            problem.put("status", 403);
            problem.put("code", "password_change_required");
            problem.put("detail", "Change your password before using the rest of the API.");
            problem.put("errors", List.of());
            objectMapper.writeValue(response.getWriter(), problem);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isAllowed(String uri) {
        return uri.startsWith("/api/v1/auth/") || uri.startsWith("/actuator/health");
    }
}
