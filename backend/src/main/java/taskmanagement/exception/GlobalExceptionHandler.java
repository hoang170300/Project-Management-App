package taskmanagement.exception;

import taskmanagement.dto.Response.ApiResponse;
import taskmanagement.dto.Response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 * TUẦN 6: Centralized exception handling
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException → 404 NOT FOUND
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.of("RESOURCE_NOT_FOUND", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), error);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle InvalidStatusTransitionException → 400 BAD REQUEST
     */
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidStatusTransition(InvalidStatusTransitionException ex) {
        ErrorResponse error = ErrorResponse.of("INVALID_STATUS_TRANSITION", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle BusinessRuleException → 409 CONFLICT
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRule(BusinessRuleException ex) {
        ErrorResponse error = ErrorResponse.of("BUSINESS_RULE_VIOLATION", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), error);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handle Validation Errors → 400 BAD REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse error = ErrorResponse.validationError(errors);
        ApiResponse<Void> response = ApiResponse.error("Validation failed", error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle IllegalArgumentException → 400 BAD REQUEST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = ErrorResponse.of("INVALID_ARGUMENT", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle all other exceptions → 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        ErrorResponse error = ErrorResponse.of("INTERNAL_SERVER_ERROR", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error("An unexpected error occurred", error);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
