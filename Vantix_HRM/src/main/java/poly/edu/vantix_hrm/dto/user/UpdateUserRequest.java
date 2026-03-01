package poly.edu.vantix_hrm.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import poly.edu.vantix_hrm.entity.User;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    private String password; // optional

    @NotNull(message = "Please select role")
    private Integer roleId;

    @NotNull(message = "Status is required")
    private User.UserStatus status;
}