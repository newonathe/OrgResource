package edu.orgresource.extended.api;

import java.util.Set;

import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

@Component
public class C1RequestValidator {
    private final Validator validator;

    public C1RequestValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(Object request) {
        Set<ConstraintViolation<Object>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new C1ValidationException();
        }
    }
}
