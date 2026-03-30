package poly.edu.vantix_hrm.dto.user;

import lombok.*;
import poly.edu.vantix_hrm.entity.User.UserStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private UserStatus status;
    private LocalDateTime lastActive;

    // Chỉ trả id + name của Role, tránh vòng lặp JSON
    private Long roleId;
    private String roleName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}