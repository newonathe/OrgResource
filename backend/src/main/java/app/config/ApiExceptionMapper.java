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
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.CannotSerializeTransactionException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
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
        if (isRecognizedLockContention(error)) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorView("RETRY_REQUIRED", "The operation encountered temporary database contention; reload and retry deliberately.")).build();
        }
        if (isConstraintViolation(error, "uk_department_name_key")) {
            return Response.status(Response.Status.CONFLICT).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorView("DUPLICATE_NAME", "A department with this name already exists.")).build();
        }
        if (isConstraintViolation(error, "uk_resource_name_key")) {
            return Response.status(Response.Status.CONFLICT).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorView("DUPLICATE_NAME", "A Resource with this name already exists.")).build();
        }
        if (isConstraintViolation(error, "uk_requirement_project_resource")) {
            return Response.status(Response.Status.CONFLICT).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorView("DUPLICATE_REQUIREMENT",
                            "This Project already has a Requirement for that Resource.")).build();
        }
        if (isConstraintViolation(error, "fk_project_department")
                || isConstraintViolation(error, "fk_requirement_project")
                || isConstraintViolation(error, "fk_requirement_resource")
                || isConstraintViolation(error, "fk_allocation_requirement")) {
            return Response.status(Response.Status.CONFLICT).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorView("DEPENDENCY_EXISTS", "The record is still referenced by another record.")).build();
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

    private boolean isConstraintViolation(Throwable error, String expectedConstraint) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException) {
                String constraint = ((org.hibernate.exception.ConstraintViolationException) cause).getConstraintName();
                return constraint != null && (expectedConstraint.equalsIgnoreCase(constraint)
                        || constraint.toLowerCase(java.util.Locale.ROOT)
                                .endsWith("." + expectedConstraint.toLowerCase(java.util.Locale.ROOT)));
            }
        }
        return false;
    }

    private boolean isRecognizedLockContention(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof PessimisticLockingFailureException
                    || cause instanceof CannotAcquireLockException
                    || cause instanceof DeadlockLoserDataAccessException
                    || cause instanceof CannotSerializeTransactionException
                    || cause instanceof org.hibernate.exception.LockAcquisitionException) {
                return true;
            }
            if (cause instanceof java.sql.SQLException) {
                java.sql.SQLException sql = (java.sql.SQLException) cause;
                if ("40001".equals(sql.getSQLState()) || sql.getErrorCode() == 1205 || sql.getErrorCode() == 1213) {
                    return true;
                }
            }
        }
        return false;
    }
}
