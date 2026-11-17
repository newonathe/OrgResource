package edu.orgresource.c0.config;

import org.apache.bval.jsr.ApacheValidationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

@Configuration
public class C0ValidationConfiguration {
    @Bean(destroyMethod = "close")
    ValidatorFactory c0JavaxValidatorFactory() {
        return Validation.byProvider(ApacheValidationProvider.class)
                .configure()
                .buildValidatorFactory();
    }

    @Bean
    Validator c0JavaxValidator(ValidatorFactory c0JavaxValidatorFactory) {
        return c0JavaxValidatorFactory.getValidator();
    }
}
