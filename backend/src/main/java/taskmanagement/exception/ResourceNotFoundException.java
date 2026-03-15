package taskmanagement.exception;


/**
 * Exception cho trường hợp không tìm thấy resource
 * TUẦN 5: Business Logic
 * TUẦN 6: Will be handled by GlobalExceptionHandler → 404
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }
}
