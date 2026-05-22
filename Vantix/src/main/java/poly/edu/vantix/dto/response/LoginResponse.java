package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private boolean success;

    private String message;

    private String accessToken;

    private String tokenType;

    private long expiresIn;

    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {

        private Long id;

        private String username;

        private String email;

        private String role;

        private Long employeeId;

        private Long departmentId;

        private String departmentName;

        private List<String> permissions;
    }
}
