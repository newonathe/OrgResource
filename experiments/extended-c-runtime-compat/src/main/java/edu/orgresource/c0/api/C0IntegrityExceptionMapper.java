package edu.orgresource.c0.api;

import org.springframework.dao.DataIntegrityViolationException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class C0IntegrityExceptionMapper implements ExceptionMapper<DataIntegrityViolationException> {
    @Override
    public Response toResponse(DataIntegrityViolationException exception) {
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiDtos.ErrorView("DEPENDENCY_OR_DUPLICATE", "The requested data conflicts with stored records."))
                .build();
    }
}
