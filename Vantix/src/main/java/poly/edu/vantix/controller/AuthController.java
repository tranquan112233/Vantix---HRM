package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.vantix.dto.request.ForgotPasswordRequest;
import poly.edu.vantix.dto.request.LoginRequest;
import poly.edu.vantix.dto.request.ResetPasswordRequest;
import poly.edu.vantix.dto.request.VerifyOtpRequest;
import poly.edu.vantix.dto.response.ForgotPasswordResponse;
import poly.edu.vantix.dto.response.LoginResponse;
import poly.edu.vantix.dto.response.VerifyOtpResponse;
import poly.edu.vantix.exception.UnauthorizedException;
import poly.edu.vantix.security.JwtUserPrincipal;
import poly.edu.vantix.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    // Lấy thông tin user hiện tại từ JWT
    @GetMapping("/me")
    public ResponseEntity<LoginResponse.UserInfo> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal principal)) {
            throw new UnauthorizedException("You are not signed in");
        }
        return ResponseEntity.ok(authService.currentUser(principal.getId()));
    }
}
