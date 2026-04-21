package poly.edu.vantix.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.ForgotPasswordRequest;
import poly.edu.vantix.dto.request.LoginRequest;
import poly.edu.vantix.dto.request.ResetPasswordRequest;
import poly.edu.vantix.dto.request.VerifyOtpRequest;
import poly.edu.vantix.dto.response.ForgotPasswordResponse;
import poly.edu.vantix.dto.response.LoginResponse;
import poly.edu.vantix.dto.response.VerifyOtpResponse;
import poly.edu.vantix.entity.PasswordResetOtp;
import poly.edu.vantix.entity.Permission;
import poly.edu.vantix.entity.Role;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.entity.enums.UserStatus;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.exception.UnauthorizedException;
import poly.edu.vantix.repository.PasswordResetOtpRepository;
import poly.edu.vantix.repository.UserRepository;
import poly.edu.vantix.security.JwtService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class AuthService {

    private static final int OTP_EXPIRES_IN_MINUTES = 10;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.auth.expose-dev-otp:false}")
    private boolean exposeDevOtp;

    public AuthService(
            UserRepository userRepository,
            PasswordResetOtpRepository passwordResetOtpRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            MailService mailService
    ) {
        this.userRepository = userRepository;
        this.passwordResetOtpRepository = passwordResetOtpRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailService = mailService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String usernameOrEmail = request.getUsernameOrEmail().trim();

        User user = userRepository.findFirstByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new BusinessException(
                        "usernameOrEmail",
                        "Account does not exist"
                ));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("password", "Password is incorrect");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Account is locked");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setLastLogin(now);
        user.setLastActive(now);

        String accessToken = jwtService.generateAccessToken(user);

        return LoginResponse.builder()
                .success(true)
                .message("Signed in successfully")
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .user(buildUserInfo(user))
                .build();
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String usernameOrEmail = request.getUsernameOrEmail().trim();
        User user = userRepository.findFirstByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElse(null);

        if (user == null) {
            return ForgotPasswordResponse.builder()
                    .success(true)
                    .message("If the account exists, an OTP has been sent")
                    .expiresInMinutes(OTP_EXPIRES_IN_MINUTES)
                    .build();
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            return ForgotPasswordResponse.builder()
                    .success(true)
                    .message("If the account exists, an OTP has been sent")
                    .expiresInMinutes(OTP_EXPIRES_IN_MINUTES)
                    .build();
        }

        String otp = "%06d".formatted(secureRandom.nextInt(1_000_000));
        PasswordResetOtp resetOtp = new PasswordResetOtp();
        resetOtp.setUser(user);
        resetOtp.setRequestId(UUID.randomUUID().toString());
        resetOtp.setOtpHash(passwordEncoder.encode(otp));
        resetOtp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRES_IN_MINUTES));

        PasswordResetOtp saved = passwordResetOtpRepository.save(resetOtp);
        mailService.sendPasswordResetOtp(user, otp, OTP_EXPIRES_IN_MINUTES);

        return ForgotPasswordResponse.builder()
                .success(true)
                .message(mailService.isEnabled() ? "OTP has been sent to email" : "OTP has been generated")
                .requestId(saved.getRequestId())
                .expiresInMinutes(OTP_EXPIRES_IN_MINUTES)
                .devOtp(exposeDevOtp ? otp : null)
                .build();
    }

    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        PasswordResetOtp resetOtp = passwordResetOtpRepository.findByRequestIdAndDeletedFalse(request.getRequestId())
                .orElseThrow(() -> new BusinessException("requestId", "OTP request does not exist"));

        ensureOtpUsable(resetOtp);

        if (!passwordEncoder.matches(request.getOtp().trim(), resetOtp.getOtpHash())) {
            resetOtp.setAttempts(resetOtp.getAttempts() + 1);
            passwordResetOtpRepository.save(resetOtp);
            throw new BusinessException("otp", "OTP is incorrect");
        }

        resetOtp.setVerifiedAt(LocalDateTime.now());
        resetOtp.setResetToken(UUID.randomUUID().toString());
        passwordResetOtpRepository.save(resetOtp);

        return VerifyOtpResponse.builder()
                .success(true)
                .message("OTP verified")
                .resetToken(resetOtp.getResetToken())
                .build();
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetOtp resetOtp = passwordResetOtpRepository.findByResetTokenAndDeletedFalse(request.getResetToken())
                .orElseThrow(() -> new BusinessException("resetToken", "Reset token is invalid"));

        if (resetOtp.getUsedAt() != null || resetOtp.getVerifiedAt() == null || resetOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Reset token is expired or already used");
        }

        User user = resetOtp.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetOtp.setUsedAt(LocalDateTime.now());
        resetOtp.setDeleted(true);
        resetOtp.setDeletedAt(LocalDateTime.now());
        passwordResetOtpRepository.save(resetOtp);
    }

    @Transactional(readOnly = true)
    public LoginResponse.UserInfo currentUser(Long userId) {
        User user = userRepository.findActiveWithRoleAndPermissionsById(userId)
                .orElseThrow(() -> new UnauthorizedException("User is no longer available"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is locked");
        }

        return buildUserInfo(user);
    }

    private LoginResponse.UserInfo buildUserInfo(User user) {
        Role role = user.getRole();
        boolean hasActiveRole = role != null && !Boolean.TRUE.equals(role.getDeleted());
        List<String> permissions = !hasActiveRole || role.getPermissions() == null
                ? List.of()
                : role.getPermissions().stream()
                        .map(Permission::getName)
                        .filter(Objects::nonNull)
                        .sorted()
                        .toList();

        return LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(hasActiveRole ? role.getName() : null)
                .permissions(permissions)
                .build();
    }

    private void ensureOtpUsable(PasswordResetOtp resetOtp) {
        if (resetOtp.getUsedAt() != null) {
            throw new BusinessException("OTP request is already used");
        }
        if (resetOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("OTP is expired");
        }
        if (resetOtp.getAttempts() >= MAX_OTP_ATTEMPTS) {
            throw new BusinessException("OTP attempt limit exceeded");
        }
    }
}
