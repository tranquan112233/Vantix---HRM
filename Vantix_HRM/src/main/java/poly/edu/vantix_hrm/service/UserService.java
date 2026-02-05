package poly.edu.vantix_hrm.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================
    // GET ALL USERS
    // =========================
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // =========================
    // GET USER BY ID
    // =========================
    public User findById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User not found"
                        ));
    }

    // =========================
    // CREATE USER (BCrypt)
    // =========================
    public User create(User user) {

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Username already exists"
            );
        }

        if (user.getEmail() != null &&
                userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already exists"
            );
        }

        // ❗ Không tin dữ liệu từ client
        user.setId(null);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setCreatedAt(null);
        user.setLastLogin(null);

        // 🔐 BCrypt password
        user.setPasswordHash(
                passwordEncoder.encode(user.getPasswordHash())
        );

        return userRepository.save(user);
    }

    // =========================
    // UPDATE USER (KHÔNG ĐỔI PASSWORD, KHÔNG ĐỔI STATUS)
    // =========================
    public User update(Integer id, User user) {
        User existing = findById(id);

        if (!existing.getUsername().equals(user.getUsername())
                && userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Username already exists"
            );
        }

        if (user.getEmail() != null &&
                !user.getEmail().equals(existing.getEmail())
                && userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already exists"
            );
        }

        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        existing.setRole(user.getRole());

        // ❌ KHÔNG cho đổi password ở đây
        // ❌ KHÔNG cho đổi status ở đây

        return userRepository.save(existing);
    }

    // =========================
    // TOGGLE LOCK / UNLOCK USER
    // =========================
    public void lock(Integer id) {
        User user = findById(id);

        user.setStatus(
                user.getStatus() == User.UserStatus.ACTIVE
                        ? User.UserStatus.LOCKED
                        : User.UserStatus.ACTIVE
        );

        userRepository.save(user);
    }

    // =========================
    // CHANGE PASSWORD (BCrypt)
    // =========================
    public void changePassword(Integer id, String newPassword) {
        User user = findById(id);

        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);
    }

    // =========================
    // DELETE USER
    // =========================
    public void delete(Integer id) {
        User user = findById(id);
        userRepository.delete(user);
    }
}
