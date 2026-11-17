package edu.orgresource.extended.api;

import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.security.C1RequestPaths;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Normalizes Jersey/Jackson malformed request errors into the API's stable JSON contract. */
@Provider
public class C1BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {
    @Context private HttpServletRequest request;

    @Override
    public Response toResponse(BadRequestException exception) {
        if (C1RequestPaths.path(request).startsWith("/api/v2/"))
            return C1HttpResponses.error(400, "INVALID_REQUEST_BODY", "The request body is malformed or invalid.", request);
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiDtos.ErrorView("INVALID_REQUEST_BODY", "The request body is malformed or invalid."))
                .build();
    }
}
