package com.kamatos.codegenvalidationdemo.validation;

import org.springframework.validation.Validator;

/**
 * Marker interface to represent only validators intended to be used for ItemsController.
 * This approach allows Spring to automatically discover and register validators for specific controllers.
 */
public interface ItemsControllerValidator extends Validator {
}
