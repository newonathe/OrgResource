package edu.orgresource.c0.api;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class C0ApiExceptionMapper implements ExceptionMapper<C0ApiException> {
    @Override
    public Response toResponse(C0ApiException exception) {
        return Response.status(exception.getStatus())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiDtos.ErrorView(exception.getCode(), exception.getMessage()))
                .build();
    }
}
