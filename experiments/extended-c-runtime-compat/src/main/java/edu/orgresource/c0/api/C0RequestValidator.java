package edu.orgresource.c0.api;

import java.util.Set;

import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

@Component
public class C0RequestValidator {
    private final Validator validator;

    public C0RequestValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(Object request) {
        Set<ConstraintViolation<Object>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new C0ValidationException();
        }
    }
}
