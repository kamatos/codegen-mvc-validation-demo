package com.kamatos.codegenvalidationdemo.exception;

import com.kamatos.codegenvalidationdemo.api.model.ValidationError;
import com.kamatos.codegenvalidationdemo.api.model.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class ValidationErrorHandler {

    @ExceptionHandler(CustomValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleCustomValidationException(CustomValidationException ex) {
        ValidationErrorResponse response = new ValidationErrorResponse();
        response.setErrors(ex.getErrors());
        return response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleMethodArgumentException(MethodArgumentNotValidException ex) {
        return buildValidationResponse(ex.getBindingResult());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleMethodValidationException(HandlerMethodValidationException ex) {
        ValidationErrorResponse response = new ValidationErrorResponse();

        ex.getParameterValidationResults().forEach(validationResult -> {
            validationResult.getResolvableErrors().forEach(error -> {
                ValidationError validationError = new ValidationError()
                        .code(buildErrorCodeFromResolvable(error))
                        .message(error.getDefaultMessage());
                response.addErrorsItem(validationError);
            });
        });

        return response;
    }

    /*
    To return field error codes in form of <constraint_code>.<field_name> in order to have simple and determined
    validation response for FE.
     */
    private ValidationErrorResponse buildValidationResponse(BindingResult bindingResult) {
        ValidationErrorResponse response = new ValidationErrorResponse();

        bindingResult.getAllErrors().stream()
                .map(error -> new ValidationError().code(buildErrorCode(error)).message(error.getDefaultMessage()))
                .forEach(response::addErrorsItem);

        return response;
    }

    private String buildErrorCodeFromResolvable(MessageSourceResolvable error) {
        if (Objects.requireNonNull(error) instanceof FieldError fieldError) {
            return buildErrorCode(fieldError);
        }
        return error.getDefaultMessage();
    }


    private String buildErrorCode(ObjectError error) {
        return switch (error) {
            case FieldError fe -> fe.getCode() + "." + fe.getField();
            default -> error.getCode();
        };
    }
}
