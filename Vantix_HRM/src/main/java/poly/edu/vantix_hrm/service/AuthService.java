package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.dto.auth.*;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.entity.VerificationToken;
import poly.edu.vantix_hrm.exception.BusinessException;
import poly.edu.vantix_hrm.repository.UserRepository;
import poly.edu.vantix_hrm.repository.VerificationTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    // =====================================================
    // LOGIN
    // =====================================================

    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BusinessException("email","Email not found"));

        boolean isMatch = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!isMatch) {
            throw new BusinessException("password","Wrong password");
        }

        if (user.getStatus() == User.UserStatus.LOCKED) {
            throw new BusinessException(
                    "general",
                    "Account is locked. Please contact admin."
            );
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        String token = jwtService.generateToken(user);

        return new LoginResponse(token);
    }


    // =====================================================
    // FORGOT PASSWORD
    // =====================================================

    public String forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BusinessException("email","Email not found"));

        String otp = String.valueOf(
                (int) ((Math.random() * 900000) + 100000)
        );

        VerificationToken otpToken = new VerificationToken();

        otpToken.setUser(user);
        otpToken.setToken(otp);
        otpToken.setTokenType(VerificationToken.TokenType.OTP);
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpToken.setUsed(false);

        tokenRepository.save(otpToken);

        emailService.sendOtpEmail(user.getEmail(), otp);

        // KHÔNG return OTP
        return "OTP sent to email";
    }


    // =====================================================
    // VERIFY OTP
    // =====================================================

    public String verifyOtp(VerifyOtpRequest request) {

        VerificationToken otpToken =
                tokenRepository
                        .findByTokenAndTokenTypeAndUsedFalse(
                                request.getOtp(),
                                VerificationToken.TokenType.OTP
                        )
                        .orElseThrow(() ->
                                new BusinessException("otp","Invalid OTP"));

        if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("otp","OTP expired");
        }

        if (!otpToken.getUser().getEmail()
                .equals(request.getEmail())) {

            throw new BusinessException("email","Email mismatch");
        }

        otpToken.setUsed(true);

        tokenRepository.save(otpToken);

        String resetToken = UUID.randomUUID().toString();

        VerificationToken resetPasswordToken =
                new VerificationToken();

        resetPasswordToken.setUser(otpToken.getUser());
        resetPasswordToken.setToken(resetToken);
        resetPasswordToken.setTokenType(
                VerificationToken.TokenType.RESET_PASSWORD
        );
        resetPasswordToken.setExpiresAt(
                LocalDateTime.now().plusMinutes(10)
        );
        resetPasswordToken.setUsed(false);

        tokenRepository.save(resetPasswordToken);

        return resetToken;
    }


    // =====================================================
    // RESET PASSWORD
    // =====================================================

    public String resetPassword(
            ResetPasswordRequest request
    ) {

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new BusinessException(
                    "confirmPassword","Passwords do not match"
            );
        }

        VerificationToken resetToken =
                tokenRepository
                        .findByTokenAndTokenTypeAndUsedFalse(
                                request.getResetToken(),
                                VerificationToken.TokenType.RESET_PASSWORD
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "token","Invalid reset token"
                                ));

        if (resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new BusinessException(
                    "token","Reset token expired"
            );
        }

        User user = resetToken.getUser();

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        resetToken.setUsed(true);

        tokenRepository.save(resetToken);

        return "Password reset successfully";
    }

}