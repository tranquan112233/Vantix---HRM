package poly.edu.vantix_hrm.dto.auth;

import lombok.*;
import poly.edu.vantix_hrm.entity.User;

/*
 * LoginResponseDTO
 * -------------------------------------------
 * Trả về cho FE sau khi đăng nhập thành công
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;
    private Long userId;
    private String username;
    private String email;
    private String roleName;

    public static LoginResponseDTO from(User user, String token) {
        return LoginResponseDTO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .build();
    }
}