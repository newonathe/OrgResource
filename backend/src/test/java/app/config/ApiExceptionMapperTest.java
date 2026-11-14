package app.config;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import javax.ws.rs.core.Response;

import org.junit.Test;

import app.dto.ApiDtos.ErrorView;

public class ApiExceptionMapperTest {
    private final ApiExceptionMapper mapper = new ApiExceptionMapper();

    @Test
    public void mapsDepartmentNameUniqueConstraintToConflict() {
        org.hibernate.exception.ConstraintViolationException duplicate =
                new org.hibernate.exception.ConstraintViolationException(
                        "duplicate department name", new SQLException("duplicate"), "uk_department_name_key");

        Response response = mapper.toResponse(new RuntimeException("save failed", duplicate));

        assertEquals(409, response.getStatus());
        ErrorView body = (ErrorView) response.getEntity();
        assertEquals("DUPLICATE_NAME", body.code);
    }

    @Test
    public void mapsJerseyNotFoundToJsonNotFound() {
        Response response = mapper.toResponse(new javax.ws.rs.NotFoundException());

        assertEquals(404, response.getStatus());
        ErrorView body = (ErrorView) response.getEntity();
        assertEquals("NOT_FOUND", body.code);
    }

    @Test
    public void mapsMalformedJerseyPathParameterToJsonBadRequest() {
        org.glassfish.jersey.server.ParamException error =
                new org.glassfish.jersey.server.ParamException.PathParamException(
                        new NumberFormatException("not-a-number"), "id", "not-a-number");

        Response response = mapper.toResponse(error);

        assertEquals(400, response.getStatus());
        ErrorView body = (ErrorView) response.getEntity();
        assertEquals("VALIDATION_FAILED", body.code);
    }

}
