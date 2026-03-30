package poly.edu.vantix_hrm.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/*
 * ResetPasswordRequestDTO
 * -------------------------------------------
 * Nhận email + OTP + mật khẩu mới để đặt lại mật khẩu
 *
 * Yêu cầu gửi lại OTP để đảm bảo đúng người đang reset
 */
@Data
public class ResetPasswordRequestDTO {
    @NotBlank(message = "Reset token is required!")
    private String token;

    @NotBlank(message = "New password is required!")
    private String newPassword;
}