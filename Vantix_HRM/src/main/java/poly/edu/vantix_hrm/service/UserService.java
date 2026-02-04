package poly.edu.vantix_hrm.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Lấy danh sách user
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // Lấy user theo id
    public User findById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User not found"
                        ));
    }

    // Tạo user mới
    public User create(User user) {

        // 👉 Luật nghiệp vụ nằm ở đây
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

        return userRepository.save(user);
    }

    // Update user
    public User update(Integer id, User user) {
        User existing = findById(id);

        // Không cho đổi username trùng
        if (!existing.getUsername().equals(user.getUsername())
                && userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Username already exists"
            );
        }

        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        existing.setRole(user.getRole());
        existing.setStatus(user.getStatus());

        return userRepository.save(existing);
    }

    // Khóa user
    public void lock(Integer id) {
        User user = findById(id);
        user.setStatus(User.UserStatus.LOCKED);
        userRepository.save(user);
    }

    // Xóa user
    public void delete(Integer id) {
        User user = findById(id);
        userRepository.delete(user);
    }
}
