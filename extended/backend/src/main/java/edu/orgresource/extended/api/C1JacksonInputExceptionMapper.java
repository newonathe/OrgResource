package edu.orgresource.extended.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.security.C1RequestPaths;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Hides parser internals and returns the stable API validation shape for malformed JSON. */
@Provider
public class C1JacksonInputExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    @Context private HttpServletRequest request;

    @Override
    public Response toResponse(JsonProcessingException exception) {
        if (C1RequestPaths.path(request).startsWith("/api/v2/"))
            return C1HttpResponses.error(400, "INVALID_REQUEST_BODY", "The request body is malformed or invalid.", request);
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiDtos.ErrorView("INVALID_REQUEST_BODY", "The request body is malformed or invalid."))
                .build();
    }
}
