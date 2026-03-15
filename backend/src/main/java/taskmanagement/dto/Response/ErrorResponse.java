package taskmanagement.dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Error Response DTO
 * TUẦN 6: Exception Handling
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String code;
    private String details;
    private Map<String, String> validationErrors;

    public static ErrorResponse of(String code, String details) {
        return ErrorResponse.builder()
                .code(code)
                .details(details)
                .build();
    }

    public static ErrorResponse validationError(Map<String, String> errors) {
        return ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .validationErrors(errors)
                .build();
    }
}
