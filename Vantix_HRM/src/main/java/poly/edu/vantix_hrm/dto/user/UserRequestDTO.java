package poly.edu.vantix_hrm.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import poly.edu.vantix_hrm.entity.User.UserStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDTO {

    @NotBlank(message = "Username is required!")
    private String username;

    // Không @NotBlank — cho phép null khi update (Service tự xử lý)
    @Size(min = 6, message = "Password must be at least 6 characters!")
    private String password;

    @NotBlank(message = "Email is required!")
    @Email(message = "Email is invalid!")
    private String email;

    @NotNull(message = "Please select role!")
    private Long roleId;

    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
}