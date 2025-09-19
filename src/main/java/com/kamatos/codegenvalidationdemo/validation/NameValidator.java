package com.kamatos.codegenvalidationdemo.validation;

import com.kamatos.codegenvalidationdemo.api.model.ValidationError;
import lombok.experimental.UtilityClass;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class NameValidator {

    public static List<ValidationError> validateNameIfTest(String name) {
        List<ValidationError> result = new ArrayList<>();
        if ("test".equalsIgnoreCase(name)) {
            result.add(new ValidationError().code("name.nonTest").message("Name cannot be Test"));
        }

        return result;
    }

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
