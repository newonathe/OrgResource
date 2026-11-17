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
public class C1ApiExceptionMapper implements ExceptionMapper<C1ApiException> {
    @Context private HttpServletRequest request;
    @Override
    public Response toResponse(C1ApiException exception) {
        if (request != null && C1RequestPaths.path(request).startsWith("/api/v2/"))
            return C1HttpResponses.error(exception.getStatus(), exception.getCode(), exception.getMessage(), request);
        return Response.status(exception.getStatus())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiDtos.ErrorView(exception.getCode(), exception.getMessage()))
                .build();
    }
}
