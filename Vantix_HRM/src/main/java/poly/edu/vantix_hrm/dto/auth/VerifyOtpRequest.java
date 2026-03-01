package poly.edu.vantix_hrm.dto.auth;

import lombok.Data;

@Data
public class VerifyOtpRequest {

    private String email;

    private String otp;

}