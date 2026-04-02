package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.auth.*;
import poly.edu.vantix_hrm.entity.AuthToken;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.entity.Permission;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.exception.BusinessException;
import poly.edu.vantix_hrm.repository.AuthTokenRepository;
import poly.edu.vantix_hrm.repository.EmployeeRepository;
import poly.edu.vantix_hrm.repository.UserRepository;
import poly.edu.vantix_hrm.security.JwtService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository      userRepository;
    private final EmployeeRepository  employeeRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder     passwordEncoder;
    private final JwtService          jwtService;
    private final EmailService        emailService;

    private static final int OTP_EXPIRY_MINUTES = 5;

    // ─── Đăng nhập ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new BusinessException(
                        "usernameOrEmail", "User not found!", HttpStatus.NOT_FOUND));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new BusinessException("usernameOrEmail", "User not found!", HttpStatus.NOT_FOUND);
        }

        if (user.getStatus() == User.UserStatus.LOCKED) {
            throw new BusinessException("usernameOrEmail", "User is locked!", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("password", "Invalid password!", HttpStatus.UNAUTHORIZED);
        }

        user.setLastActive(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getId());
        return LoginResponseDTO.from(user, token);
    }

    // ─── Lấy thông tin user hiện tại (dùng cho /auth/me sau F5) ──────────────
    // Trả UserProfileResponse (có permissions) thay vì LoginResponseDTO

    @Transactional(readOnly = true)
    public UserProfileResponse me(Long userId) {
        User user = userRepository.findByIdWithRoleAndPermissions(userId)
                .orElseThrow(() -> new BusinessException(
                        "user", "User not found!", HttpStatus.NOT_FOUND));

        if (Boolean.TRUE.equals(user.getDeleted()) || user.getStatus() == User.UserStatus.LOCKED) {
            throw new BusinessException("user", "Invalid User", HttpStatus.FORBIDDEN);
        }

        // Lấy danh sách permission từ role (điều chỉnh theo entity thực tế của bạn)
        List<String> permissions = user.getRole() != null
                ? user.getRole().getPermissions().stream()
                .map(Permission::getName)
                .toList()
                : List.of();

        Long employeeId = employeeRepository.findByUserIdAndDeletedFalse(user.getId())
                .map(Employee::getId)
                .orElse(null);

        return UserProfileResponse.builder()
                .id(user.getId())
                .employeeId(employeeId)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .permissions(permissions)
                .build();
    }

    // ─── Quên mật khẩu ───────────────────────────────────────────────────────

    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(
                        "email", "Email not found", HttpStatus.NOT_FOUND));

        authTokenRepository.deleteAllByUserAndType(user, AuthToken.TokenType.OTP);

        String otp = generateOtp();

        AuthToken authToken = AuthToken.builder()
                .user(user)
                .token(otp)
                .type(AuthToken.TokenType.OTP)
                .expiryDate(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .used(false)
                .build();

        authTokenRepository.save(authToken);
        emailService.sendOtp(user.getEmail(), otp);
    }

    // ─── Xác thực OTP ────────────────────────────────────────────────────────

    @Transactional
    public String verifyOtp(VerifyOtpRequestDTO request) {
        User user = findUserByEmail(request.getEmail());
        AuthToken otpToken = findAndValidateOtp(user, request.getOtp());

        // ❌ đánh dấu OTP đã dùng
        otpToken.setUsed(true);
        authTokenRepository.save(otpToken);

        // ❗ tạo reset token
        String resetToken = generateResetToken();

        AuthToken resetAuthToken = AuthToken.builder()
                .user(user)
                .token(resetToken)
                .type(AuthToken.TokenType.RESET_PASSWORD)
                .expiryDate(LocalDateTime.now().plusMinutes(10)) // reset token sống lâu hơn OTP
                .used(false)
                .build();

        authTokenRepository.save(resetAuthToken);

        return resetToken; // trả về cho FE
    }

    // ─── Đặt lại mật khẩu ────────────────────────────────────────────────────

    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {

        AuthToken token = authTokenRepository
                .findTopByTokenAndTypeAndUsedFalseOrderByCreatedAtDesc(
                        request.getToken(),
                        AuthToken.TokenType.RESET_PASSWORD
                )
                .orElseThrow(() -> new BusinessException(
                        "token", "Invalid token", HttpStatus.BAD_REQUEST));

        if (!token.isValid()) {
            throw new BusinessException("token", "Token has expired or already been used", HttpStatus.BAD_REQUEST);
        }

        User user = token.getUser();

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        authTokenRepository.save(token);
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        "email", "Email not found", HttpStatus.NOT_FOUND));
    }

    private AuthToken findAndValidateOtp(User user, String otp) {
        AuthToken authToken = authTokenRepository
                .findTopByUserAndTypeAndUsedFalseOrderByCreatedAtDesc(user, AuthToken.TokenType.OTP)
                .orElseThrow(() -> new BusinessException(
                        "otp", "Invalid OTP", HttpStatus.BAD_REQUEST));

        if (LocalDateTime.now().isAfter(authToken.getExpiryDate())) {
            throw new BusinessException("otp", "OTP has expired", HttpStatus.BAD_REQUEST);
        }

        if (!authToken.getToken().equals(otp)) {
            throw new BusinessException("otp", "Incorrect OTP", HttpStatus.BAD_REQUEST);
        }

        return authToken;
    }

    private String generateOtp() {
        int otp = 100000 + new Random().nextInt(900000);
        return String.valueOf(otp);
    }

    private String generateResetToken() {
        return java.util.UUID.randomUUID().toString();
    }
}