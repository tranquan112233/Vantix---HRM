package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {

    private boolean success;
    private String message;
    private String requestId;
    private Integer expiresInMinutes;
    private String devOtp;
}
