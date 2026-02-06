package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.DTO.LoginRequest;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername());

        if (user == null) {
            return ResponseEntity.status(401)
                    .body("Sai tài khoản hoặc mật khẩu");
        }

        if (!user.getPasswordHash()
                .equals(request.getPassword())) {

            return ResponseEntity.status(401)
                    .body("Sai tài khoản hoặc mật khẩu");
        }

        return ResponseEntity.ok(user);
    }
}
