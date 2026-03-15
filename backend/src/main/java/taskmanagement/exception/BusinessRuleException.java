package taskmanagement.exception;

/**
 * Exception cho business rule violations
 * TUẦN 5: Business Logic
 * TUẦN 6: Will be handled by GlobalExceptionHandler → 409 Conflict
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
