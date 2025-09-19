package com.kamatos.codegenvalidationdemo.validation;

import com.kamatos.codegenvalidationdemo.api.model.ValidatedUpdateItemRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class ValidatedUpdateItemRequestValidator implements ItemsControllerValidator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ValidatedUpdateItemRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidatedUpdateItemRequest request = (ValidatedUpdateItemRequest) target;

        NameValidator.validateName(request.getName(), errors);
    }
}
