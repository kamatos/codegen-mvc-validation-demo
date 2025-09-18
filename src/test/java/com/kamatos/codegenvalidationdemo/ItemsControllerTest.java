package com.kamatos.codegenvalidationdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamatos.codegenvalidationdemo.api.model.CreateItemRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static java.lang.String.format;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        request.setEmail("email");

        // When & Then
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void createItem_WithTestNameAndNullableEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateItemRequest request = new CreateItemRequest();
        request.setName(INVALID_NAME);

        // When & Then
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpectAll(errorMatcher(0, "NotNull.email", "must not be null"))
                .andExpectAll(errorMatcher(1, "nonTest.name", "Name cannot be Test"));
    }

    private static ResultMatcher[] errorMatcher(int index, String code, String message) {
        return new ResultMatcher[] {
                jsonPath(format("$.errors[%s].code", index)).value(code),
                jsonPath(format("$.errors[%s].message", index)).value(message)
        };
    }
}
