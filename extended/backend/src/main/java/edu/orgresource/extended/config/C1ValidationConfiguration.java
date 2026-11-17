package edu.orgresource.extended.config;

import org.apache.bval.jsr.ApacheValidationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

@Configuration
public class C1ValidationConfiguration {
    @Bean(destroyMethod = "close")
    ValidatorFactory c1JavaxValidatorFactory() {
        return Validation.byProvider(ApacheValidationProvider.class)
                .configure()
                .buildValidatorFactory();
    }

    @Bean
    Validator c1JavaxValidator(ValidatorFactory c1JavaxValidatorFactory) {
        return c1JavaxValidatorFactory.getValidator();
    }
}
