package poly.edu.vantix_hrm.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/*
 * ForgotPasswordRequestDTO
 * -------------------------------------------
 * Nhận email để gửi OTP đặt lại mật khẩu
 */
@Data
public class ForgotPasswordRequestDTO {

    @NotBlank(message = "Email is required!")
    @Email(message = "Email is invalid!")
    private String email;
}