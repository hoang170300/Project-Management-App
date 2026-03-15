package taskmanagement.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Auth Response DTO (after login/register)
 * TUẦN 7: Authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    // SỬA LỖI TẠI ĐÂY: Thêm @Builder.Default để Lombok không bỏ qua giá trị "Bearer"
    @Builder.Default
    private String type = "Bearer";

    private Long userId;
    private String username;
    private String email;
    private String fullName;

    /**
     * Tối ưu phương thức static factory
     */
    public static AuthResponse of(String token, Long userId, String username, String email, String fullName) {
        return AuthResponse.builder()
                .token(token)
                // Không cần .type("Bearer") nữa vì đã có @Builder.Default ở trên
                .userId(userId)
                .username(username)
                .email(email)
                .fullName(fullName)
                .build();
    }
}