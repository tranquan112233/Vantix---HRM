package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.dto.LoginRequest;
import poly.edu.vantix_hrm.dto.LoginResponse;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.entity.User.UserStatus;
import poly.edu.vantix_hrm.repository.UserRepository;
import poly.edu.vantix_hrm.security.JwtUtil;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest req) {

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Sai tài khoản"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().getRoleName()
        );

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole().getRoleName()
        );
    }
}
