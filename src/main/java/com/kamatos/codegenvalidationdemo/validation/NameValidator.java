package com.kamatos.codegenvalidationdemo.validation;

import lombok.experimental.UtilityClass;
import org.springframework.validation.Errors;

@UtilityClass
public final class NameValidator {

    public static void validateName(String name, Errors errors) {

        if (name == null) {
            errors.rejectValue("name", "required", "Name is required");
            return;
        }

        if ("test".equalsIgnoreCase(name)) {
            errors.rejectValue("name", "nonTest", "Name cannot be Test");
        }
    }

}
