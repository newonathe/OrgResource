package edu.orgresource.extended.api;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.security.C1RequestPaths;

@Provider
public class C1FallbackExceptionMapper implements ExceptionMapper<Throwable> {
    @Context private HttpServletRequest request;
    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof jakarta.ws.rs.NotFoundException) {
            if (C1RequestPaths.path(request).startsWith("/api/v2/"))
                return C1HttpResponses.error(404, "NOT_FOUND", "The requested resource was not found.", request);
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ApiDtos.ErrorView("NOT_FOUND", "The requested resource was not found."))
                    .build();
        }
        if (C1RequestPaths.path(request).startsWith("/api/v2/"))
            return C1HttpResponses.error(500, "INTERNAL_ERROR", "The request could not be completed.", request);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiDtos.ErrorView("INTERNAL_ERROR", "The request could not be completed."))
                .build();
    }
}
