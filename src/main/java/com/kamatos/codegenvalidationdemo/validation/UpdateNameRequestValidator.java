package com.kamatos.codegenvalidationdemo.validation;

import com.kamatos.codegenvalidationdemo.api.model.UpdateNameRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class UpdateNameRequestValidator implements ItemsControllerValidator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdateNameRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UpdateNameRequest request = (UpdateNameRequest) target;

        NameValidator.validateName(request.getName(), errors);
    }
}
