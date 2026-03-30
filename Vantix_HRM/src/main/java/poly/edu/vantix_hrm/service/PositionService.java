package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.page.PageRequestDTO;
import poly.edu.vantix_hrm.dto.page.PageResponseDTO;
import poly.edu.vantix_hrm.dto.position.PositionRequestDTO;
import poly.edu.vantix_hrm.dto.position.PositionResponseDTO;
import poly.edu.vantix_hrm.entity.Department;
import poly.edu.vantix_hrm.entity.Position;
import poly.edu.vantix_hrm.exception.BusinessException;
import poly.edu.vantix_hrm.repository.DepartmentRepository;
import poly.edu.vantix_hrm.repository.PositionRepository;
import poly.edu.vantix_hrm.utils.BaseSpecification;
import poly.edu.vantix_hrm.utils.PageHelper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;

    // ─── Lấy danh sách phân trang + lọc động ─────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponseDTO<PositionResponseDTO> getAll(String keyword, PageRequestDTO pageRequest) {
        Specification<Position> spec = Specification
                .where(BaseSpecification.<Position>search(keyword, "name", "description"))
                .and(BaseSpecification.isNotDeleted());

        return PageHelper.toResponse(
                positionRepository.findAll(spec, PageHelper.createPageable(pageRequest))
                        .map(this::toResponse)
        );
    }

    // ─── Lấy danh sách position theo department ──────────────────────────────

    @Transactional(readOnly = true)
    public List<PositionResponseDTO> getByDepartment(Long departmentId) {
        // Kiểm tra department tồn tại
        findDepartmentById(departmentId);

        return positionRepository.findByDepartmentIdAndDeletedFalse(departmentId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Lấy chi tiết 1 position ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PositionResponseDTO getById(Long id) {
        return toResponse(findById(id));
    }

    // ─── Tạo mới ────────────────────────────────────────────────────────────

    public PositionResponseDTO create(PositionRequestDTO request) {
        Department department = findDepartmentById(request.getDepartmentId());

        // Kiểm tra tên đã tồn tại trong phòng ban
        if (positionRepository.existsByNameAndDepartmentIdAndDeletedFalse(request.getName(), request.getDepartmentId())) {
            throw new BusinessException("name", "Tên chức vụ đã tồn tại trong phòng ban này!", HttpStatus.BAD_REQUEST);
        }

        Position position = Position.builder()
                .name(request.getName())
                .description(request.getDescription())
                .department(department)
                .build();

        return toResponse(positionRepository.save(position));
    }

    // ─── Cập nhật ───────────────────────────────────────────────────────────

    public PositionResponseDTO update(Long id, PositionRequestDTO request) {
        Position position = findById(id);
        Department department = findDepartmentById(request.getDepartmentId());

        // Kiểm tra tên trùng (bỏ qua chính nó)
        if (!position.getName().equals(request.getName()) &&
                positionRepository.existsByNameAndDepartmentIdAndIdNotAndDeletedFalse(
                        request.getName(), request.getDepartmentId(), id)) {
            throw new BusinessException("name", "Tên chức vụ đã tồn tại trong phòng ban này!", HttpStatus.BAD_REQUEST);
        }

        position.setName(request.getName());
        position.setDescription(request.getDescription());
        position.setDepartment(department);

        return toResponse(positionRepository.save(position));
    }

    // ─── Xóa mềm ────────────────────────────────────────────────────────────

    public void delete(Long id) {
        Position position = findById(id);

        // Kiểm tra có nhân viên không
        long employeeCount = positionRepository.countEmployeesByPositionId(id);
        if (employeeCount > 0) {
            throw new BusinessException("id", "Không thể xóa chức vụ đang có nhân viên!", HttpStatus.BAD_REQUEST);
        }

        position.setDeleted(true);
        positionRepository.save(position);
    }

    // ─── Helper ─────────────────────────────────────────────────────────────

    private Position findById(Long id) {
        return positionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("id", "Không tìm thấy chức vụ!", HttpStatus.NOT_FOUND));
    }

    private Department findDepartmentById(Long id) {
        return departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("departmentId", "Không tìm thấy phòng ban!", HttpStatus.NOT_FOUND));
    }

    // ─── Map Entity → Response DTO ──────────────────────────────────────────

    private PositionResponseDTO toResponse(Position position) {
        return PositionResponseDTO.builder()
                .id(position.getId())
                .name(position.getName())
                .description(position.getDescription())
                .departmentId(position.getDepartment() != null ? position.getDepartment().getId() : null)
                .departmentName(position.getDepartment() != null ? position.getDepartment().getName() : null)
                .employeeCount((int) positionRepository.countEmployeesByPositionId(position.getId()))
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .createdBy(position.getCreatedBy())
                .updatedBy(position.getUpdatedBy())
                .build();
    }
}