package com.kamatos.codegenvalidationdemo.exception;

import com.kamatos.codegenvalidationdemo.api.model.ValidationError;
import lombok.Getter;

import java.util.List;

@Getter
public class CustomValidationException extends RuntimeException {
    private final List<ValidationError> errors;

    public CustomValidationException(List<ValidationError> errors) {
        this.errors = errors;
    }
}
