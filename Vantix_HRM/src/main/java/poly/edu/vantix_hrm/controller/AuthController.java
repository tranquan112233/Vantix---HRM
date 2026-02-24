package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.ForgotPasswordRequest;
import poly.edu.vantix_hrm.dto.LoginRequest;
import poly.edu.vantix_hrm.dto.LoginResponse;
import poly.edu.vantix_hrm.dto.ResetPasswordRequest;
import poly.edu.vantix_hrm.service.AuthService;
import poly.edu.vantix_hrm.service.PasswordResetService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    // ... existing code ...

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        passwordResetService.requestReset(req.getEmail());
        // luôn trả OK để tránh dò email
        return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại, hệ thống đã gửi mã xác nhận"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        passwordResetService.resetPassword(req.getEmail(), req.getCode(), req.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công"));
    }
}
