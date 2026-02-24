package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import poly.edu.vantix_hrm.dto.ChangePasswordRequest;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.repository.UserRepository;
import poly.edu.vantix_hrm.security.JwtUtil;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@CrossOrigin
public class AccountController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ChangePasswordRequest req
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(UNAUTHORIZED, "Thiếu token");
        }

        String token = authorization.substring("Bearer ".length()).trim();
        String username;
        try {
            username = jwtUtil.getUsername(token);
        } catch (Exception e) {
            throw new ResponseStatusException(UNAUTHORIZED, "Token không hợp lệ");
        }

        if (req.getCurrentPassword() == null || req.getCurrentPassword().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Thiếu mật khẩu hiện tại");
        }
        if (req.getNewPassword() == null || req.getNewPassword().length() < 6) {
            throw new ResponseStatusException(BAD_REQUEST, "Mật khẩu mới tối thiểu 6 ký tự");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User không tồn tại"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(BAD_REQUEST, "Mật khẩu hiện tại không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }

}