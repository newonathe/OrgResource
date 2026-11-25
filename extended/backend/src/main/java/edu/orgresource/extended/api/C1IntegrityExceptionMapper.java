package edu.orgresource.extended.api;

import org.springframework.dao.DataIntegrityViolationException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import edu.orgresource.extended.identity.C1HttpResponses;
import edu.orgresource.extended.security.C1RequestPaths;

@Provider
public class C1IntegrityExceptionMapper implements ExceptionMapper<DataIntegrityViolationException> {
    @Context private HttpServletRequest request;
    @Override
    public Response toResponse(DataIntegrityViolationException exception) {
        if (C1RequestPaths.path(request).startsWith("/api/v2/"))
            return C1HttpResponses.error(409, "DATA_CONFLICT", "The request conflicts with existing organization data.", request);
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiDtos.ErrorView("DEPENDENCY_OR_DUPLICATE", "The requested data conflicts with stored records."))
                .build();
    }
}
