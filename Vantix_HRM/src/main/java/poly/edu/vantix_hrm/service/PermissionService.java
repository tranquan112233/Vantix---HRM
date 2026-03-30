package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.page.PageRequestDTO;
import poly.edu.vantix_hrm.dto.page.PageResponseDTO;
import poly.edu.vantix_hrm.dto.permission.*;
import poly.edu.vantix_hrm.entity.Permission;
import poly.edu.vantix_hrm.exception.BusinessException;
import poly.edu.vantix_hrm.repository.PermissionRepository;
import poly.edu.vantix_hrm.utils.BaseSpecification;
import poly.edu.vantix_hrm.utils.PageHelper;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;

    // ─── Lấy danh sách phân trang + lọc động ─────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponseDTO<PermissionResponseDTO> getAll(String keyword, PageRequestDTO pageRequest) {
        Specification<Permission> spec = Specification
                .where(BaseSpecification.<Permission>search(keyword, "name", "description"))
                .and(BaseSpecification.isNotDeleted());

        return PageHelper.toResponse(
                permissionRepository.findAll(spec, PageHelper.createPageable(pageRequest))
                        .map(this::toResponse)
        );
    }

    // ─── Lấy chi tiết 1 permission ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PermissionResponseDTO getById(Long id) {
        return toResponse(findById(id));
    }

    // ─── Tạo mới ──────────────────────────────────────────────────────────────

    public PermissionResponseDTO create(PermissionRequestDTO request) {
        if (permissionRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new BusinessException("name", "Permission name already exists", HttpStatus.BAD_REQUEST);
        }

        Permission permission = Permission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return toResponse(permissionRepository.save(permission));
    }

    // ─── Cập nhật ─────────────────────────────────────────────────────────────

    public PermissionResponseDTO update(Long id, PermissionRequestDTO request) {
        Permission permission = findById(id);

        if (permissionRepository.existsByNameAndIdNotAndDeletedFalse(request.getName(), id)) {
            throw new BusinessException("name", "Permission name already exists", HttpStatus.BAD_REQUEST);
        }

        permission.setName(request.getName());
        permission.setDescription(request.getDescription());

        return toResponse(permissionRepository.save(permission));
    }

    // ─── Xóa mềm ──────────────────────────────────────────────────────────────

    public void delete(Long id) {
        Permission permission = findById(id);
        permission.setDeleted(true);
        permissionRepository.save(permission);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Permission findById(Long id) {
        return permissionRepository.findById(id)
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .orElseThrow(() -> new BusinessException("id", "Permission not found", HttpStatus.NOT_FOUND));
    }

    // ─── Map Entity → Response DTO ────────────────────────────────────────────

    private PermissionResponseDTO toResponse(Permission permission) {
        return PermissionResponseDTO.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .createdBy(permission.getCreatedBy())
                .updatedBy(permission.getUpdatedBy())
                .build();
    }
}