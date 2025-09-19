package com.kamatos.codegenvalidationdemo.validation;

import com.kamatos.codegenvalidationdemo.api.model.CreateItemRequest;
import com.kamatos.codegenvalidationdemo.validation.constraint.EmailConstraintValidator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class CreateItemRequestValidator implements ItemsControllerValidator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return CreateItemRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        CreateItemRequest request = (CreateItemRequest) target;

        NameValidator.validateName(request.getName(), errors);

        if (request.getEmail() != null && !EmailConstraintValidator.NAME_LASTNAME_PATTERN.matcher(request.getEmail()).matches()) {
            errors.rejectValue("email", "format.name_lastname_required", "Email must be in format 'name.lastname@domain'");
        }
    }
}
