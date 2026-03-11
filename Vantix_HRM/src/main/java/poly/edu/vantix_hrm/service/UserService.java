package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.dto.user.*;
import poly.edu.vantix_hrm.entity.Role;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.exception.BusinessException;
import poly.edu.vantix_hrm.repository.RoleRepository;
import poly.edu.vantix_hrm.repository.UserRepository;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /* ================= FIND ================= */

    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse findById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("user","User not found"));

        return mapToResponse(user);
    }

    /* ================= CREATE ================= */

    public UserResponse create(CreateUserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("username","Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("email","Email already exists");
        }

        // Lấy danh sách Roles từ DB dựa vào List<Integer> roleIds
        List<Role> roles = roleRepository.findAllById(request.getRoleIds());

        // Kiểm tra xem số lượng quyền tìm thấy có khớp với số lượng gửi lên không
        if (roles.isEmpty() || roles.size() != request.getRoleIds().size()) {
            throw new BusinessException("role", "One or more roles not found or invalid");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // Gán danh sách quyền (Dùng HashSet để bỏ vào thuộc tính Set<Role> của Entity)
        user.setRoles(new HashSet<>(roles));
        user.setStatus(User.UserStatus.ACTIVE);

        userRepository.save(user);

        return mapToResponse(user);
    }

    /* ================= UPDATE ================= */

    public UserResponse update(Integer id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("user","User not found"));

        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {

            throw new BusinessException("username","Username already exists");
        }

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {

            throw new BusinessException("email","Email already exists");
        }

        // Lấy danh sách Roles từ DB để cập nhật
        List<Role> roles = roleRepository.findAllById(request.getRoleIds());

        if (roles.isEmpty() || roles.size() != request.getRoleIds().size()) {
            throw new BusinessException("role", "One or more roles not found or invalid");
        }

        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        // Cập nhật lại danh sách quyền
        user.setRoles(new HashSet<>(roles));
        user.setStatus(request.getStatus());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        return mapToResponse(user);
    }

    /* ================= LOCK ================= */

    public void lock(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("user","User not found"));

        user.setStatus(User.UserStatus.LOCKED);
        userRepository.save(user);
    }

    public void unlock(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("user","User not found"));

        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);
    }

    /* ================= MAPPER ================= */

    private UserResponse mapToResponse(User user) {

        // Trích xuất list ID và list Tên quyền từ Set<Role> của User
        List<Integer> roleIds = user.getRoles().stream().map(Role::getRoleId).toList();
        List<String> roleNames = user.getRoles().stream().map(Role::getRoleName).toList();

        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleIds(roleIds)      // Map mảng ID
                .roleNames(roleNames)  // Map mảng Tên quyền
                .status(user.getStatus().name())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }
}