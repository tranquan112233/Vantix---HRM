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

import java.util.ArrayList;
import java.util.Arrays;
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

        // Lấy 1 Role duy nhất từ DB
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("role", "Role not found"));

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // Gán 1 Role
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);

        // Nối mảng quyền thành chuỗi lưu vào Cột Permissions
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            user.setPermissions(String.join(",", request.getPermissions()));
        } else {
            user.setPermissions("");
        }

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

        // Lấy 1 Role từ DB để cập nhật
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("role", "Role not found"));

        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());

        // Cập nhật lại 1 Role
        user.setRole(role);
        user.setStatus(request.getStatus());

        // Cập nhật lại chuỗi Permissions
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            user.setPermissions(String.join(",", request.getPermissions()));
        } else {
            user.setPermissions("");
        }

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

        // Lấy thông tin 1 Role
        Integer roleId = user.getRole() != null ? user.getRole().getRoleId() : null;
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : null;

        // Cắt chuỗi quyền (permissions) từ DB thành mảng List<String> cho Vue.js đọc
        List<String> permissionList = new ArrayList<>();
        if (user.getPermissions() != null && !user.getPermissions().trim().isEmpty()) {
            permissionList = Arrays.asList(user.getPermissions().split(","));
        }

        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleId(roleId)            // Trả về 1 ID Role
                .roleName(roleName)        // Trả về 1 Tên Role
                .permissions(permissionList) // Trả về mảng quyền riêng
                .status(user.getStatus().name())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }
}