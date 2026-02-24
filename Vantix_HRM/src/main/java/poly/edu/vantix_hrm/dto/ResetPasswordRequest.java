package poly.edu.vantix_hrm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    private String email;
    private String code;       // 6 digits
    private String newPassword;
}
