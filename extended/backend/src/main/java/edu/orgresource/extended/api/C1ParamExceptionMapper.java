package edu.orgresource.extended.api;

import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.security.C1RequestPaths;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ParamException;

/** Maps unparseable Jersey path/query parameters to a controlled client error. */
@Provider
public class C1ParamExceptionMapper implements ExceptionMapper<ParamException> {
    @Context private HttpServletRequest request;

    @Override
    public Response toResponse(ParamException exception) {
        if (C1RequestPaths.path(request).startsWith("/api/v2/"))
            return C1HttpResponses.error(400, "INVALID_PATH_PARAMETER", "A path parameter has an invalid format.", request);
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiDtos.ErrorView("INVALID_PATH_PARAMETER", "A path parameter has an invalid format."))
                .build();
    }
}
