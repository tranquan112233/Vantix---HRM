package poly.edu.vantix.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.RoleRequest;
import poly.edu.vantix.dto.response.PageResponse;
import poly.edu.vantix.dto.response.PermissionResponse;
import poly.edu.vantix.dto.response.RoleResponse;
import poly.edu.vantix.entity.Permission;
import poly.edu.vantix.entity.Role;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.repository.PermissionRepository;
import poly.edu.vantix.repository.RoleRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    // Danh sách role với tìm kiếm
    @Transactional(readOnly = true)
    public List<RoleResponse> search(String keyword) {
        return roleRepository.search(keyword)
                .stream()
                .map(RoleResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> searchPage(String keyword, Pageable pageable) {
        return PageResponse.from(roleRepository.search(keyword, pageable), RoleResponse::fromEntity);
    }

    // Lấy chi tiết role
    @Transactional(readOnly = true)
    public RoleResponse getById(Long id) {
        Role role = findActiveById(id);
        return RoleResponse.fromEntity(role);
    }

    // Tạo mới role
    @Transactional
    public RoleResponse create(RoleRequest request) {
        if (roleRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new BusinessException("name", "Role name already exists");
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        // Gán permissions
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>(
                    permissionRepository.findAllById(request.getPermissionIds())
            );
            role.setPermissions(permissions);
        }

        role = roleRepository.save(role);
        return RoleResponse.fromEntity(role);
    }

    // Cập nhật role
    @Transactional
    public RoleResponse update(Long id, RoleRequest request) {
        Role role = findActiveById(id);

        // Kiểm tra tên trùng (trừ chính nó)
        roleRepository.findByName(request.getName())
                .filter(r -> !r.getId().equals(id) && !r.getDeleted())
                .ifPresent(r -> {
                    throw new BusinessException("name", "Role name already exists");
                });

        role.setName(request.getName());
        role.setDescription(request.getDescription());

        // Cập nhật permissions
        if (request.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>(
                    permissionRepository.findAllById(request.getPermissionIds())
            );
            role.setPermissions(permissions);
        }

        role = roleRepository.save(role);
        return RoleResponse.fromEntity(role);
    }

    // Xóa mềm role
    @Transactional
    public void delete(Long id) {
        Role role = findActiveById(id);
        role.setDeleted(true);
        role.setDeletedAt(LocalDateTime.now());
        roleRepository.save(role);
    }

    // Danh sách tất cả permission
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll()
                .stream()
                .filter(p -> !p.getDeleted())
                .map(PermissionResponse::fromEntity)
                .toList();
    }

    // --- Helper ---

    private Role findActiveById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Role not found with id: " + id));
        if (role.getDeleted()) {
            throw new BusinessException("Role has been deleted");
        }
        return role;
    }
}
