package poly.edu.vantix_hrm.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    // Đổi từ Integer roleId -> List<Integer> roleIds
    // Dùng @NotEmpty thay vì @NotNull để bắt buộc mảng không được rỗng
    @NotEmpty(message = "Please select at least one role")
    private List<Integer> roleIds;
}