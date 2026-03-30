package poly.edu.vantix_hrm.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/*
 * LoginRequestDTO
 * -------------------------------------------
 * Đăng nhập bằng username hoặc email
 */
@Data
public class LoginRequestDTO {

    @NotBlank(message = "Username and email address is required!")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required!")
    private String password;
}