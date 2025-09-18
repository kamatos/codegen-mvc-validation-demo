package com.kamatos.codegenvalidationdemo.validation;

import com.kamatos.codegenvalidationdemo.api.model.CreateItemRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class CreateItemRequestValidator implements ItemsControllerValidator {

    @Override
    public boolean supports(Class<?> clazz) {
        return CreateItemRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CreateItemRequest request = (CreateItemRequest) target;

        NameValidator.validateName(request.getName(), errors);
    }
}
