package poly.edu.vantix_hrm.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import poly.edu.vantix_hrm.entity.User;

import java.util.List;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    private String password; // optional

    // Đổi từ Integer roleId -> List<Integer> roleIds
    @NotEmpty(message = "Please select at least one role")
    private Integer roleId;

    @NotNull(message = "Status is required")
    private User.UserStatus status;

    private List<String> permissions;
}