package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.UserStatus;

@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must be at most 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    // Mật khẩu bắt buộc khi tạo mới, optional khi update
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Long roleId;

    private UserStatus status;
}
