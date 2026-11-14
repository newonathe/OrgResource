package app.components;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RequestValidation {
    @Autowired private Validator validator;

    public <T> void validate(T request) {
        if (request == null) {
            throw new ApiException(400, "VALIDATION_FAILED", "A JSON request body is required.");
        }
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ApiException(400, "VALIDATION_FAILED", "One or more request fields are invalid.");
        }
    }
}
