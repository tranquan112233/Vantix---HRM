package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import poly.edu.vantix_hrm.entity.PasswordResetToken;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.repository.PasswordResetTokenRepository;
import poly.edu.vantix_hrm.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    private static final SecureRandom random = new SecureRandom();

    public void requestReset(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Email không được để trống");
        }

        // Rate-limit: 60 giây / lần (theo email)
        tokenRepository.findTopByEmailOrderByCreatedAtDesc(email.trim().toLowerCase())
                .ifPresent(latest -> {
                    if (latest.getLastSentAt() != null) {
                        LocalDateTime nextAllowed = latest.getLastSentAt().plusSeconds(60);
                        if (LocalDateTime.now().isBefore(nextAllowed)) {
                            throw new ResponseStatusException(TOO_MANY_REQUESTS, "Vui lòng chờ 60 giây để gửi lại mã");
                        }
                    }
                });

        // Không leak email tồn tại hay không
        boolean exists = userRepository.existsByEmail(email.trim().toLowerCase());
        if (!exists) {
            // vẫn trả OK ở controller
            return;
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        String codeHash = passwordEncoder.encode(code);

        PasswordResetToken token = PasswordResetToken.builder()
                .email(email.trim().toLowerCase())
                .codeHash(codeHash)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .lastSentAt(LocalDateTime.now())
                .build();

        tokenRepository.save(token);

        mailService.sendResetCode(email.trim().toLowerCase(), code);
    }

    public void resetPassword(String email, String code, String newPassword) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Email không hợp lệ");
        }
        if (code == null || !code.matches("^\\d{6}$")) {
            throw new ResponseStatusException(BAD_REQUEST, "Mã phải gồm 6 chữ số");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new ResponseStatusException(BAD_REQUEST, "Mật khẩu mới tối thiểu 6 ký tự");
        }

        PasswordResetToken token = tokenRepository
                .findTopByEmailAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(email.trim().toLowerCase(), LocalDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Mã không đúng hoặc đã hết hạn"));

        if (!passwordEncoder.matches(code, token.getCodeHash())) {
            throw new ResponseStatusException(BAD_REQUEST, "Mã không đúng hoặc đã hết hạn");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Email không tồn tại"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);
    }
}
