package poly.edu.vantix_hrm.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Integer id;
    private String username;
    private String email;
    private Integer roleId;
    private String roleName;
    private String status;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}