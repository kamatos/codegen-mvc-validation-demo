package com.kamatos.codegenvalidationdemo.exception;

import com.kamatos.codegenvalidationdemo.api.model.ValidationError;
import com.kamatos.codegenvalidationdemo.api.model.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleMethodArgumentException(MethodArgumentNotValidException ex) {
        return buildValidationResponse(ex.getBindingResult());
    }

//    @ExceptionHandler(HandlerMethodValidationException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ValidationErrorResponse handleMethodValidationException(HandlerMethodValidationException ex) {
//        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
//        problem.setTitle("Validation failure");
//        problem.setDetail(ex.getReason());
//        return problem;
//    }

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

    private String buildErrorCode(ObjectError error) {
        return switch (error) {
            case FieldError fe -> fe.getCode() + "." + fe.getField();
            default -> error.getCode();
        };
    }
}
