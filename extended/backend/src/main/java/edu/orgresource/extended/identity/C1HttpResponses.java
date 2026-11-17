package edu.orgresource.extended.identity;

import java.util.UUID;

import edu.orgresource.extended.security.C1RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Response;

public final class C1HttpResponses {
    private C1HttpResponses() { }

    public static String requestId(HttpServletRequest request) {
        Object value = request.getAttribute(C1RequestIdFilter.ATTRIBUTE);
        return value instanceof String text ? text : UUID.randomUUID().toString();
    }

    public static Response envelope(int status, Object data, HttpServletRequest request, boolean noStore) {
        Response.ResponseBuilder response = Response.status(status)
                .entity(new IdentityDtos.Envelope<>(data, requestId(request)))
                .header("X-Request-ID", requestId(request));
        if (noStore) {
            CacheControl cache = new CacheControl(); cache.setNoStore(true);
            response.cacheControl(cache).header("Pragma", "no-cache");
        }
        return response.build();
    }

    public static Response error(int status, String code, String message, HttpServletRequest request) {
        CacheControl cache = new CacheControl(); cache.setNoStore(true);
        return Response.status(status).type(jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE)
                .entity(new IdentityDtos.Error(code, message, requestId(request)))
                .header("X-Request-ID", requestId(request)).cacheControl(cache).header("Pragma", "no-cache").build();
    }

    public static Response empty(int status) { return Response.status(status).build(); }
}
