package com.kamatos.codegenvalidationdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamatos.codegenvalidationdemo.api.model.CreateItemRequest;
import com.kamatos.codegenvalidationdemo.api.model.UpdateItemRequest;
import com.kamatos.codegenvalidationdemo.api.model.ValidatedUpdateItemRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.UUID;

import static java.lang.String.format;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemsControllerTest {
    public static final String INVALID_NAME = "test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createItem_WithValidRequest_ShouldReturnOk() throws Exception {
        // Given
        CreateItemRequest request = new CreateItemRequest();
        request.setName("name");
        request.setEmail("my.email@domain.com");

        // When & Then
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Test to represent the case with the following requirements:
     * - Field `email` in request body is validated during data binding and according to basic Jakarta
     * annotations(NotNull)
     * - Field `name` iin request body is validated by custom validator and fails according to custom business
     * rules(cannot be Test)
     * - All errors are returned at once in a single error response.
     */
    @Test
    void createItem_WithTestNameAndNullableEmail_ShouldReturnBadRequest_AllErrors() throws Exception {
        // Given
        CreateItemRequest request = new CreateItemRequest();
        request.setName(INVALID_NAME);

        // When & Then
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpectAll(errorMatcher(0, "NotNull.email", "must not be null"))
                .andExpectAll(errorMatcher(1, "nonTest.name", "Name cannot be Test"));
    }

    @Test
    void getItems_WithCustomConstraintOnEmail_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/items").param("email", "dummy@test.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpectAll(errorMatcher(0, "email.format.name_lastname_required", "email.format.name_lastname_required"))
                .andExpectAll(errorMatcher(1, "email.domain.blocked", "email.domain.blocked"));
    }

    @Test
    void updateItem_ShowcaseSeparatedValidations_NullEmail_ShouldReturnBadRequest_OnlyOneError() throws Exception {
        // Given
        var request = new UpdateItemRequest();
        request.setName(INVALID_NAME);

        // When & Then
        mockMvc.perform(put("/api/items/{id}", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpectAll(errorMatcher(0, "NotNull.email", "must not be null"));
    }

    @Test
    void updateItem_ShowcaseSeparatedValidations_ValidEmailInvalidName_ShouldReturnBadRequest_OnlyOneError() throws Exception {
        // Given
        var request = new UpdateItemRequest();
        request.setName(INVALID_NAME);
        request.setEmail("my-email@domain.com");

        // When & Then
        mockMvc.perform(put("/api/items/{id}", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpectAll(errorMatcher(0, "name.nonTest", "Name cannot be Test"));
    }

    @Test
    void updateItem_ShowcaseJoinedValidations_NullEmail_ShouldReturnBadRequest_AllErrors() throws Exception {
        // Given
        var request = new ValidatedUpdateItemRequest();
        request.setName(INVALID_NAME);

        // When & Then
        mockMvc.perform(patch("/api/items/{id}", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpectAll(errorMatcher(0, "NotNull.email", "must not be null"))
                .andExpectAll(errorMatcher(1, "nonTest.name", "Name cannot be Test"));
    }

    private static ResultMatcher[] errorMatcher(int index, String code, String message) {
        return new ResultMatcher[]{
                jsonPath(format("$.errors[%s].code", index)).value(code),
                jsonPath(format("$.errors[%s].message", index)).value(message)
        };
    }
}
