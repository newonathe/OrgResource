package edu.orgresource.extended.security;

import jakarta.servlet.http.HttpServletRequest;

public final class C1RequestPaths {
    private C1RequestPaths() { }
    public static String path(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        return uri.startsWith(context) ? uri.substring(context.length()) : uri;
    }
}
