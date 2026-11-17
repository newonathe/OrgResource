package edu.orgresource.c0.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class C0SecurityResponses {
    private C0SecurityResponses() { }

    public static void unauthenticated(HttpServletRequest request, HttpServletResponse response,
                                       org.springframework.security.core.AuthenticationException exception)
            throws IOException {
        write(response, 401, "UNAUTHENTICATED", "Authentication is required.");
    }

    public static void forbidden(HttpServletRequest request, HttpServletResponse response,
                                 org.springframework.security.access.AccessDeniedException exception)
            throws IOException {
        write(response, 403, "FORBIDDEN", "Access is denied or a valid CSRF token is required.");
    }

    private static void write(HttpServletResponse response, int status, String code, String message) throws IOException {
        if (response.isCommitted()) return;
        response.resetBuffer();
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }
}
