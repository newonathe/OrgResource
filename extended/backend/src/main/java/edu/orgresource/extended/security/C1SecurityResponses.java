package edu.orgresource.extended.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class C1SecurityResponses {
    private C1SecurityResponses() { }

    public static void unauthenticated(HttpServletRequest request, HttpServletResponse response,
                                       AuthenticationException exception) throws IOException {
        write(request, response, 401, "AUTH_REQUIRED", "Authentication is required.");
    }

    public static void forbidden(HttpServletRequest request, HttpServletResponse response,
                                 AccessDeniedException exception) throws IOException {
        write(request, response, 403, "FORBIDDEN", "Access is denied or a valid CSRF token is required.");
    }

    public static void write(HttpServletRequest request, HttpServletResponse response,
                             int status, String code, String message) throws IOException {
        if (response.isCommitted()) return;
        String requestId = String.valueOf(request.getAttribute(C1RequestIdFilter.ATTRIBUTE));
        if ("null".equals(requestId)) requestId = java.util.UUID.randomUUID().toString();
        response.resetBuffer();
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Request-ID", requestId);
        response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message
                + "\",\"requestId\":\"" + requestId + "\"}");
    }
}
