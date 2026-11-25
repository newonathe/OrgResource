package edu.orgresource.extended.security;

import java.io.IOException;
import java.net.URI;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public final class C1SameOriginFilter extends OncePerRequestFilter {
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        boolean safe = "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
        return safe || !C1RequestPaths.path(request).startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        if (!sameOrigin(origin, request)) {
            C1SecurityResponses.write(request, response, 403, "ORIGIN_INVALID", "A same-origin request is required.");
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean sameOrigin(String raw, HttpServletRequest request) {
        if (raw == null || raw.isBlank()) return false;
        try {
            URI origin = URI.create(raw);
            if (origin.getUserInfo() != null || origin.getHost() == null || origin.getPath() != null && !origin.getPath().isEmpty()
                    || origin.getQuery() != null || origin.getFragment() != null) return false;
            String scheme = request.getScheme();
            int requestPort = request.getServerPort();
            int originPort = origin.getPort() >= 0 ? origin.getPort()
                    : "https".equalsIgnoreCase(origin.getScheme()) ? 443 : 80;
            int effectiveRequestPort = requestPort >= 0 ? requestPort
                    : "https".equalsIgnoreCase(scheme) ? 443 : 80;
            return scheme.equalsIgnoreCase(origin.getScheme())
                    && request.getServerName().equalsIgnoreCase(origin.getHost())
                    && effectiveRequestPort == originPort;
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }
}
