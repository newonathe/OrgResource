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
public class C1ValidationExceptionMapper implements ExceptionMapper<C1ValidationException> {
    @Context private HttpServletRequest request;
    @Override
    public Response toResponse(C1ValidationException exception) {
        if (C1RequestPaths.path(request).startsWith("/api/v2/"))
            return C1HttpResponses.error(400, "VALIDATION_FAILED", exception.getMessage(), request);
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiDtos.ErrorView("VALIDATION_FAILED", exception.getMessage()))
                .build();
    }
}
