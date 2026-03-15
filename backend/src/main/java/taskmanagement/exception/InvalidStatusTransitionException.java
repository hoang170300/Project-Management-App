package taskmanagement.exception;


import taskmanagement.enums.TaskStatus;

/**
 * Exception cho invalid status transitions
 * TUẦN 5: IMPROVEMENT #1 - Status Flow Validation
 * TUẦN 6: Will be handled by GlobalExceptionHandler → 400 Bad Request
 */
public class InvalidStatusTransitionException extends BusinessRuleException {

    public InvalidStatusTransitionException(TaskStatus from, TaskStatus to) {
        super(String.format("Cannot transition task status from %s to %s", from, to));
    }

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}