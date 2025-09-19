# Codegen MVC Validation Demo

This project demonstrates a clean approach to integrating custom validation with OpenAPI code generation while keeping generated POJOs free from custom annotations.

## Problem Statement

When using OpenAPI Generator with the `spring-boot` library, extending validation capabilities for DTOs presents several challenges:

### 1. Custom Constraints Approach

**Method**: Add custom constraints directly to generated POJOs using `x-field-extra-annotation`.

```yaml
# OpenAPI specification
properties:
  name:
    type: string
    minLength: 1
    maxLength: 30
    x-field-extra-annotation: "@com.myconstraints.NameValidator"
```

**Generated Code**:
```java
@Generated(/*...*/)
public class CreateItemRequest {
    @com.myconstraints.NameValidator
    private String name;
    private String email;
    // getters, setters, boilerplate code
}
```

**Problems**:
- Language-specific details leak into generic API contract
- API contract becomes dependent on annotation implementation
- May cause compilation errors in client applications if implementation is missing

### 2. Manual Validation Approach

**Method**: Perform validation outside Spring MVC validation framework.

```java
public class MyResourceController implements MyResourceApi {
    record CreateMyResourceRequest(@NotNull String email, String name) {}

    @Override
    public ResponseEntity<MyResourceResponse> createMyResource(CreateMyResourceRequest request) {
        if (NameValidator.validateNameNotTest(request.getName())) {
            throw new ValidationException("Test name is not allowed");
        }
        // business logic
    }
}
```

**Problems**:
- **N+1 Validation Problem**: If one field fails validation, other fields are not validated
- **Incomplete Error Responses**: Only the first validation error is returned
- **Poor User Experience**: Users must fix errors one at a time

**Example**: See test `updateItem_ShowcaseSeparatedValidations_NullEmail_ShouldReturnBadRequest_OnlyOneError` - when email is null, name validation is skipped.

## Solution

### 1. Request Body Validation Strategy

**Key Principles**:
1. Register validators per class using `@InitBinder`
2. Use Spring MVC validation (`@Valid`) instead of Bean Validation (`@Validated`)
3. Enrich MVC validation errors with custom business rules

**Why Spring MVC over Bean Validation**:
- Bean Validation fails immediately on first constraint violation
- Spring MVC validation collects ALL errors before responding
- Enables comprehensive error reporting in a single response

**Example**: See test `updateItem_ShowcaseJoinedValidations_NullEmail_ShouldReturnBadRequest_AllErrors` - both email and name validation errors are returned together.

### 2. Parameter Validation Strategy

**Method**: Apply custom constraints directly to method parameters in controller implementation.

```java
@Override
public ResponseEntity<List<ItemResponse>> getItems(@ValidEmail String email) {
    return ResponseEntity.ok(null);
}
```

**Configuration Required**:
```java
@Bean
public ValidationConfigurationCustomizer allowParameterConstraintOverrideCustomizer() {
    return (configuration) -> configuration.addProperty(
            BaseHibernateValidatorConfiguration.ALLOW_PARAMETER_CONSTRAINT_OVERRIDE, "true");
}
```

## Architecture

### Project Structure

```
src/main/java/com/kamatos/codegenvalidationdemo/
├── CodegenMvcValidationDemoApplication.java    # Main application class
├── controller/
│   └── ItemsController.java                    # Controller implementing generated interface
├── exception/
│   └── ValidationExceptionHandler.java         # Global validation error handler
└── validation/
    ├── ItemsControllerValidator.java           # Marker interface for validators
    ├── ItemsValidatorsRegistrar.java           # Automatic validator registration
    ├── NameValidator.java                      # Reusable validation utilities
    ├── CreateItemRequestValidator.java         # Custom validator for create requests
    └── constraint/
        ├── ValidEmail.java                     # Custom email constraint annotation
        └── EmailConstraintValidator.java       # Email validation logic
```

### Validation Flow

1. **Request Processing**: Request arrives at controller method
2. **Automatic Validation**: Spring MVC applies registered validators via `@InitBinder`
3. **Custom Business Rules**: Validators implement business logic and add errors to `BindingResult`
4. **Error Collection**: All validation errors are collected
5. **Structured Response**: `ValidationExceptionHandler` processes errors into structured API response

### Validator Registration

```java
@InitBinder
public void initBinder(WebDataBinder binder) {
    validatorsRegistrar.initTodosControllerBinder(binder);
}
```

### Custom Validation Example

```java
@Override
public void validate(@NonNull Object target, @NonNull Errors errors) {
    CreateItemRequest request = (CreateItemRequest) target;
    
    // Reusable validation logic for name
    NameValidator.validateName(request.getName(), errors);
    
    // Validate email format using custom constraint validator
    if (request.getEmail() != null && !emailValidator.isValid(request.getEmail(), null)) {
        errors.rejectValue("email", "email.format.name_lastname_required", 
                          "Email must be in format 'name.lastname@domain'");
    }
}
```

## API Endpoints

- `GET /api/items?email={email}` - Get items with email parameter validation
- `POST /api/items` - Create item with comprehensive validation
- `PUT /api/items/{id}` - Update item (separated validation - stops on first error)
- `PATCH /api/items/{id}` - Update item (joined validation - collects all errors)

## Validation Rules

### CreateItemRequest & ValidatedUpdateItemRequest
- **Name**: 
  - Required (Jakarta `@NotNull`)
  - Cannot be "test" (case-insensitive, custom business rule)
- **Email**: 
  - Required (Jakarta `@NotNull`)
  - Must be in format "name.lastname@domain" (custom business rule)
  - Domain cannot be "test.com" (custom business rule)

### UpdateItemRequest
- **Name**: 
  - Required (Jakarta `@NotNull`)
  - Cannot be "test" (case-insensitive, custom business rule)
- **Email**: 
  - Required (Jakarta `@NotNull`)

### Query Parameters
- **Email**: Must follow "name.lastname@domain" format and domain cannot be "test.com"

## Running the Application

1. **Generate Code**:
   ```bash
   mvn generate-sources
   ```

2. **Run Application**:
   ```bash
   mvn spring-boot:run
   ```

3. **Test API**:
   ```bash
   # Valid request
   curl -X POST http://localhost:8080/api/items \
     -H "Content-Type: application/json" \
     -d '{"name": "John Doe", "email": "john.doe@example.com"}'

   # Invalid request - multiple errors (comprehensive validation)
   curl -X POST http://localhost:8080/api/items \
     -H "Content-Type: application/json" \
     -d '{"name": "test", "email": "invalid-format"}'

   # Separated validation - stops on first error
   curl -X PUT http://localhost:8080/api/items/123e4567-e89b-12d3-a456-426614174000 \
     -H "Content-Type: application/json" \
     -d '{"name": "test", "email": "invalid-format"}'

   # Joined validation - collects all errors
   curl -X PATCH http://localhost:8080/api/items/123e4567-e89b-12d3-a456-426614174000 \
     -H "Content-Type: application/json" \
     -d '{"name": "test", "email": "invalid-format"}'

   # Parameter validation
   curl -X GET "http://localhost:8080/api/items?email=invalid@test.com"
   ```

## Test Cases

The project includes comprehensive test cases demonstrating different validation scenarios:

### Test Case 1: Comprehensive Validation
**Test**: `createItem_WithTestNameAndNullableEmail_ShouldReturnBadRequest_AllErrors`
- **Purpose**: Shows comprehensive validation collecting all errors
- **Try it**:
  ```bash
  curl -X POST http://localhost:8080/api/items \
    -H "Content-Type: application/json" \
    -d '{"name": "test"}'
  ```
- **Expected**: Returns 2 errors (email required + name cannot be "test")

### Test Case 2: Separated Validation (N+1 Problem)
**Test**: `updateItem_ShowcaseSeparatedValidations_NullEmail_ShouldReturnBadRequest_OnlyOneError`
- **Purpose**: Demonstrates the N+1 validation problem
- **Try it**:
  ```bash
  curl -X PUT http://localhost:8080/api/items/123e4567-e89b-12d3-a456-426614174000 \
    -H "Content-Type: application/json" \
    -d '{"name": "test"}'
  ```
- **Expected**: Returns only 1 error (email required) - name validation is skipped

### Test Case 3: Joined Validation (Solution)
**Test**: `updateItem_ShowcaseJoinedValidations_NullEmail_ShouldReturnBadRequest_AllErrors`
- **Purpose**: Shows the solution with comprehensive error collection
- **Try it**:
  ```bash
  curl -X PATCH http://localhost:8080/api/items/123e4567-e89b-12d3-a456-426614174000 \
    -H "Content-Type: application/json" \
    -d '{"name": "test"}'
  ```
- **Expected**: Returns 2 errors (email required + name cannot be "test")

### Test Case 4: Parameter Validation
**Test**: `getItems_WithCustomConstraintOnEmail_ShouldReturnBadRequest`
- **Purpose**: Demonstrates custom parameter validation
- **Try it**:
  ```bash
  curl -X GET "http://localhost:8080/api/items?email=dummy@test.com"
  ```
- **Expected**: Returns 2 errors (invalid email format + blocked domain)

## Benefits

1. **Clean Separation**: Generated POJOs remain free from custom business logic
2. **Comprehensive Error Reporting**: All validation errors collected in single response
3. **Maintainable**: Custom validation logic centralized and reusable
4. **Testable**: Validators can be unit tested independently
5. **Flexible**: Easy to add new validators or modify existing ones
6. **User-Friendly**: Eliminates N+1 validation problem for better UX

## Request/Response Models

- **CreateItemRequest**: `{ "name": "string", "email": "string" }`
- **UpdateItemRequest**: `{ "name": "string", "email": "string" }`
- **ValidatedUpdateItemRequest**: `{ "name": "string", "email": "string" }`
- **ItemResponse**: `{ "id": "uuid", "name": "string", "email": "string" }`
- **ValidationErrorResponse**: `{ "errors": [{"code": "string", "message": "string"}] }`

## References

- **[Spring MVC Validation](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html)**  
  Official Spring Framework documentation for validation in web MVC controllers

- **[@InitBinder Annotation](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-initbinder.html)**  
  Spring Framework reference for the `@InitBinder` annotation used for custom data binding

- **[OpenAPI Generator - Spring](https://openapi-generator.tech/docs/generators/spring)**  
  Official documentation for the Spring generator in OpenAPI Generator
