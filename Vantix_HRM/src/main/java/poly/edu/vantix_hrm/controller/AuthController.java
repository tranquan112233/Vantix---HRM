package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.auth.*;
import poly.edu.vantix_hrm.security.CustomUserPrincipal;
import poly.edu.vantix_hrm.service.AuthService;

/*
 * AuthController
 * -------------------------------------------
 * BASE URL: /api/auth
 *
 * Flow quên mật khẩu:
 *   1. POST /forgot-password  → gửi OTP về email
 *   2. POST /verify-otp       → xác thực OTP, trả về resetToken (UUID)
 *   3. POST /reset-password   → đặt lại mật khẩu bằng resetToken
 *
 * PUBLIC_URLS: login, forgot-password, verify-otp, reset-password
 * AUTHENTICATED: /me
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // GET /api/auth/me — Bearer token bắt buộc
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(authService.me(principal.getUserId()));
    }

    // POST /api/auth/forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok("Mã OTP đã được gửi đến email của bạn");
    }

    // POST /api/auth/verify-otp
    // Trả về resetToken (UUID string) — FE dùng để gọi /reset-password
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody VerifyOtpRequestDTO request) {
        String resetToken = authService.verifyOtp(request);
        return ResponseEntity.ok(resetToken);
    }

    // POST /api/auth/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("Đặt lại mật khẩu thành công");
    }
}