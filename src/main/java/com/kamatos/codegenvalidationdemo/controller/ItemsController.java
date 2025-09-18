package com.kamatos.codegenvalidationdemo.controller;

import com.kamatos.codegenvalidationdemo.api.ItemsApi;
import com.kamatos.codegenvalidationdemo.api.model.CreateItemRequest;
import com.kamatos.codegenvalidationdemo.api.model.ItemResponse;
import com.kamatos.codegenvalidationdemo.api.model.UpdateNameRequest;
import com.kamatos.codegenvalidationdemo.validation.ItemsValidatorsRegistrar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.ok().body(null);
    }

    @Override
    public ResponseEntity<ItemResponse> updateItemName(UUID id, UpdateNameRequest updateNameRequest) {
        return ResponseEntity.ok().body(null);
    }
}
