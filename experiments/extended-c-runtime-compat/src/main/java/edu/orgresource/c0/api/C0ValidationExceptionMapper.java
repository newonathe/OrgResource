package edu.orgresource.c0.api;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class C0ValidationExceptionMapper implements ExceptionMapper<C0ValidationException> {
    @Override
    public Response toResponse(C0ValidationException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiDtos.ErrorView("VALIDATION_FAILED", exception.getMessage()))
                .build();
    }
}
