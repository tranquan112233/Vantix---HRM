package poly.edu.vantix_hrm.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data

public class ResetPasswordRequest {

    private String resetToken;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters long")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    @Size(min = 6, message = "Confirm password must be at least 6 characters long")
    private String confirmPassword;

}