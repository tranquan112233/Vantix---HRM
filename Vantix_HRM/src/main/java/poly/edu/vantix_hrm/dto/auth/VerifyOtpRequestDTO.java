package poly.edu.vantix_hrm.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/*
 * VerifyOtpRequestDTO
 * -------------------------------------------
 * Nhận email + mã OTP để xác thực
 */
@Data
public class VerifyOtpRequestDTO {

    @NotBlank(message = "Email is required!")
    @Email(message = "Email is invalid!")
    private String email;

    @NotBlank(message = "OTP is required!")
    private String otp;
}