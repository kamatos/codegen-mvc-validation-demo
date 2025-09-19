package com.kamatos.codegenvalidationdemo.controller;

import com.kamatos.codegenvalidationdemo.api.ItemsApi;
import com.kamatos.codegenvalidationdemo.api.model.CreateItemRequest;
import com.kamatos.codegenvalidationdemo.api.model.ItemResponse;
import com.kamatos.codegenvalidationdemo.api.model.UpdateItemRequest;
import com.kamatos.codegenvalidationdemo.api.model.ValidatedUpdateItemRequest;
import com.kamatos.codegenvalidationdemo.api.model.ValidationError;
import com.kamatos.codegenvalidationdemo.exception.CustomValidationException;
import com.kamatos.codegenvalidationdemo.validation.ItemsValidatorsRegistrar;
import com.kamatos.codegenvalidationdemo.validation.NameValidator;
import com.kamatos.codegenvalidationdemo.validation.constraint.ValidEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ItemsController implements ItemsApi {

    private final ItemsValidatorsRegistrar validatorsRegistrar;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        validatorsRegistrar.initTodosControllerBinder(binder);
    }

    @Override
    public ResponseEntity<ItemResponse> createItem(CreateItemRequest createItemRequest) {
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<List<ItemResponse>> getItems(@ValidEmail String email) {
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<ItemResponse> updateItem(UUID id, UpdateItemRequest updateItemRequest) {
        List<ValidationError> errors = NameValidator.validateNameIfTest(updateItemRequest.getName());

        if (!errors.isEmpty()) {
            throw new CustomValidationException(errors);
        }

        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<ItemResponse> validatedUpdateItem(UUID id, ValidatedUpdateItemRequest request) {
        return ResponseEntity.ok(null);
    }


}
