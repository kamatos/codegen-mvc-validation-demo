package com.kamatos.codegenvalidationdemo.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * Automatically registers validators for the TodoItemsController using Spring's @InitBinder mechanism.
 * This approach integrates custom validation with Spring MVC's validation framework,
 * allowing all validation errors to be collected in the BindingResult.
 */
@Component
@RequiredArgsConstructor
public class ItemsValidatorsRegistrar {
    private final List<ItemsControllerValidator> itemsControllerValidators;

    public void initItemsControllerBinder(WebDataBinder binder) {
        Object target = binder.getTarget();
        if (target == null) return;

        Class<?> clazz = target.getClass();

        // Register all validators that support the target class
        itemsControllerValidators.stream()
                .filter(validator -> validator.supports(clazz))
                .forEach(binder::addValidators);
    }
}
