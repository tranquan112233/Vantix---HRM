package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.page.PageRequestDTO;
import poly.edu.vantix_hrm.dto.page.PageResponseDTO;
import poly.edu.vantix_hrm.dto.role.*;
import poly.edu.vantix_hrm.entity.Permission;
import poly.edu.vantix_hrm.entity.Role;
import poly.edu.vantix_hrm.exception.BusinessException;
import poly.edu.vantix_hrm.repository.PermissionRepository;
import poly.edu.vantix_hrm.repository.RoleRepository;
import poly.edu.vantix_hrm.utils.BaseSpecification;
import poly.edu.vantix_hrm.utils.PageHelper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    // ─── Lấy danh sách phân trang + lọc động ─────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponseDTO<RoleResponseDTO> getAll(String keyword, PageRequestDTO pageRequest) {
        Specification<Role> spec = Specification
                .where(BaseSpecification.<Role>search(keyword, "name", "description"))
                .and(BaseSpecification.isNotDeleted());

        return PageHelper.toResponse(
                roleRepository.findAll(spec, PageHelper.createPageable(pageRequest))
                        .map(this::toResponse)
        );
    }

    // ─── Lấy chi tiết 1 role ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public RoleResponseDTO getById(Long id) {
        return toResponse(findById(id));
    }

    // ─── Tạo mới ──────────────────────────────────────────────────────────────

    public RoleResponseDTO create(RoleRequestDTO request) {
        if (roleRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new BusinessException("name", "Role name already exists", HttpStatus.BAD_REQUEST);
        }

        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .permissions(resolvePermissions(request.getPermissionIds()))
                .build();

        return toResponse(roleRepository.save(role));
    }

    // ─── Cập nhật ─────────────────────────────────────────────────────────────

    public RoleResponseDTO update(Long id, RoleRequestDTO request) {
        Role role = findById(id);

        if (roleRepository.existsByNameAndIdNotAndDeletedFalse(request.getName(), id)) {
            throw new BusinessException("name", "Role name already exists", HttpStatus.BAD_REQUEST);
        }

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(resolvePermissions(request.getPermissionIds()));

        return toResponse(roleRepository.save(role));
    }

    // ─── Xóa mềm ──────────────────────────────────────────────────────────────

    public void delete(Long id) {
        Role role = findById(id);
        role.setDeleted(true);
        roleRepository.save(role);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Role findById(Long id) {
        return roleRepository.findById(id)
                .filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
                .orElseThrow(() -> new BusinessException("id", "Role not found", HttpStatus.NOT_FOUND));
    }

    // Resolve danh sách Permission từ set ID
    private Set<Permission> resolvePermissions(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptySet();

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(ids));

        // Kiểm tra nếu có ID không tồn tại
        if (permissions.size() != ids.size()) {
            throw new BusinessException("permissionIds", "One or more permissions not found", HttpStatus.NOT_FOUND);
        }

        return permissions;
    }

    // ─── Map Entity → Response DTO ────────────────────────────────────────────

    private RoleResponseDTO toResponse(Role role) {
        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions().stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet()))
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .createdBy(role.getCreatedBy())
                .updatedBy(role.getUpdatedBy())
                .build();
    }
}