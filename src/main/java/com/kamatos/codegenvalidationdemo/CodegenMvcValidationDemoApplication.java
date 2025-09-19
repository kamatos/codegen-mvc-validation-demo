package com.kamatos.codegenvalidationdemo;

import org.hibernate.validator.BaseHibernateValidatorConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CodegenMvcValidationDemoApplication {
    /*
     * Enables custom constraint annotations on overridden controller methods. This allows
     * generated API interfaces to have basic constraints while controller implementations
     * can add additional custom validation constraints that aren't present in the interface.
     */
    @Bean
    public ValidationConfigurationCustomizer allowParameterConstraintOverrideCustomizer() {
        return (configuration) -> configuration.addProperty(
                BaseHibernateValidatorConfiguration.ALLOW_PARAMETER_CONSTRAINT_OVERRIDE, "true");
    }

    public static void main(String[] args) {
        SpringApplication.run(CodegenMvcValidationDemoApplication.class, args);
    }
}
