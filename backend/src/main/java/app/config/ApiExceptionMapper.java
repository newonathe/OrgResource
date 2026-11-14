package app.config;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.glassfish.jersey.server.ParamException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.components.ApiException;
import app.dto.ApiDtos.ErrorView;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionMapper.class);

    @Override
    public Response toResponse(Exception error) {
        if (error instanceof ApiException) {
            ApiException api = (ApiException) error;
            return Response.status(api.status).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorView(api.code, api.getMessage())).build();
        }
        if (isDepartmentNameKeyViolation(error)) {
            return Response.status(Response.Status.CONFLICT).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorView("DUPLICATE_NAME", "A department with this name already exists.")).build();
        }
        if (error instanceof NotFoundException) {
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorView("NOT_FOUND", "The requested route or resource was not found.")).build();
        }
        if (error instanceof ParamException) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorView("VALIDATION_FAILED", "One or more URL parameters are invalid.")).build();
        }
        if (error instanceof ConstraintViolationException || error instanceof BadRequestException) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorView("VALIDATION_FAILED", "The request is invalid.")).build();
        }
        LOG.error("Unhandled request error", error);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorView("INTERNAL_ERROR", "An unexpected server error occurred.")).build();
    }

    private boolean isDepartmentNameKeyViolation(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException) {
                String constraint = ((org.hibernate.exception.ConstraintViolationException) cause).getConstraintName();
                return constraint != null && ("uk_department_name_key".equalsIgnoreCase(constraint)
                        || constraint.toLowerCase(java.util.Locale.ROOT).endsWith(".uk_department_name_key"));
            }
        }
        return false;
    }
}
