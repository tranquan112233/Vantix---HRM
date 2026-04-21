package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {

    @NotBlank(message = "Request id is required")
    private String requestId;

    @NotBlank(message = "OTP is required")
    private String otp;
}
