package poly.edu.vantix.service;

import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.UserRequest;
import poly.edu.vantix.dto.response.PageResponse;
import poly.edu.vantix.dto.response.UserResponse;
import poly.edu.vantix.entity.Role;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.entity.enums.UserStatus;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.repository.RoleRepository;
import poly.edu.vantix.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Danh sách user với tìm kiếm
    @Transactional(readOnly = true)
    public List<UserResponse> search(String keyword) {
        return search(keyword, null, null);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> search(String keyword, Long roleId, UserStatus status) {
        return userRepository.search(keyword, roleId, status)
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchPage(String keyword, Pageable pageable) {
        return searchPage(keyword, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchPage(String keyword, Long roleId, UserStatus status, Pageable pageable) {
        return PageResponse.from(
                userRepository.search(keyword, roleId, status, pageable),
                UserResponse::fromEntity
        );
    }

    // Lấy chi tiết user
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = findActiveById(id);
        return UserResponse.fromEntity(user);
    }

    // Tạo mới user
    @Transactional
    public UserResponse create(UserRequest request) {
        // Mật khẩu bắt buộc khi tạo mới
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("password", "Password is required when creating a user");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("username", "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("email", "Email already exists");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("roleId", "Role does not exist"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE);

        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    // Cập nhật user
    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = findActiveById(id);

        // Kiểm tra email trùng (trừ chính nó)
        userRepository.findByEmail(request.getEmail())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new BusinessException("email", "Email already exists");
                });

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("roleId", "Role does not exist"));

        user.setEmail(request.getEmail());
        user.setRole(role);

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        // Chỉ cập nhật mật khẩu nếu có gửi lên
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    // Xóa mềm user
    @Transactional
    public void delete(Long id) {
        User user = findActiveById(id);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // --- Helper ---

    private User findActiveById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found with id: " + id));
        if (user.getDeleted()) {
            throw new BusinessException("User has been deleted");
        }
        return user;
    }
}
