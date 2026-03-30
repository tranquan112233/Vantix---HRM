package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.page.PageRequestDTO;
import poly.edu.vantix_hrm.dto.page.PageResponseDTO;
import poly.edu.vantix_hrm.dto.user.UserRequestDTO;
import poly.edu.vantix_hrm.dto.user.UserResponseDTO;
import poly.edu.vantix_hrm.entity.Role;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.entity.User.UserStatus;
import poly.edu.vantix_hrm.exception.BusinessException;
import poly.edu.vantix_hrm.repository.RoleRepository;
import poly.edu.vantix_hrm.repository.UserRepository;
import poly.edu.vantix_hrm.utils.BaseSpecification;
import poly.edu.vantix_hrm.utils.PageHelper;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Lấy danh sách phân trang + lọc động ─────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponseDTO<UserResponseDTO> getAll(String keyword, UserStatus status, PageRequestDTO pageRequest) {
        Specification<User> spec = Specification
                .where(BaseSpecification.<User>search(keyword, "username", "email"))
                .and(BaseSpecification.equal("status", status))
                .and(BaseSpecification.isNotDeleted());

        return PageHelper.toResponse(
                userRepository.findAll(spec, PageHelper.createPageable(pageRequest))
                        .map(this::toResponse)
        );
    }

    // ─── Lấy chi tiết 1 user ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponseDTO getById(Long id) {
        return toResponse(findById(id));
    }

    // ─── Tạo mới ──────────────────────────────────────────────────────────────

    public UserResponseDTO create(UserRequestDTO request) {
        if (userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
            throw new BusinessException("username", "Username already exists", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new BusinessException("email", "Email already exists", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(findRoleById(request.getRoleId()))
                .status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
                .build();

        return toResponse(userRepository.save(user));
    }

    // ─── Cập nhật ─────────────────────────────────────────────────────────────

    public UserResponseDTO update(Long id, UserRequestDTO request) {
        User user = findById(id);

        if (userRepository.existsByUsernameAndIdNotAndDeletedFalse(request.getUsername(), id)) {
            throw new BusinessException("username", "Username already exists", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmailAndIdNotAndDeletedFalse(request.getEmail(), id)) {
            throw new BusinessException("email", "Email already exists", HttpStatus.BAD_REQUEST);
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(findRoleById(request.getRoleId()));

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        // Chỉ đổi password nếu client gửi lên
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(userRepository.save(user));
    }

    // ─── Xóa mềm ──────────────────────────────────────────────────────────────

    public void delete(Long id) {
        User user = findById(id);
        user.setDeleted(true);
        userRepository.save(user);
    }

    // ─── Thay đổi trạng thái ACTIVE / LOCKED ─────────────────────────────────

    public UserResponseDTO changeStatus(Long id, UserStatus status) {
        User user = findById(id);
        user.setStatus(status);
        return toResponse(userRepository.save(user));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private User findById(Long id) {
        return userRepository.findById(id)
                .filter(u -> !Boolean.TRUE.equals(u.getDeleted()))
                .orElseThrow(() -> new BusinessException("id", "User not found", HttpStatus.NOT_FOUND));
    }

    private Role findRoleById(Long roleId) {
        if (roleId == null) return null;
        return roleRepository.findById(roleId)
                .filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
                .orElseThrow(() -> new BusinessException("roleId", "Role not found", HttpStatus.NOT_FOUND));
    }

    // ─── Map Entity → Response DTO ────────────────────────────────────────────

    private UserResponseDTO toResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .lastActive(user.getLastActive())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .build();
    }
}