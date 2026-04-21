package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.entity.enums.UserStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Long roleId;
    private String roleName;
    private UserStatus status;
    private LocalDateTime lastLogin;
    private LocalDateTime lastActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .roleId(u.getRole() != null ? u.getRole().getId() : null)
                .roleName(u.getRole() != null ? u.getRole().getName() : null)
                .status(u.getStatus())
                .lastLogin(u.getLastLogin())
                .lastActive(u.getLastActive())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
