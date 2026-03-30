package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.department.DepartmentRequestDTO;
import poly.edu.vantix_hrm.dto.department.DepartmentResponseDTO;
import poly.edu.vantix_hrm.dto.page.PageRequestDTO;
import poly.edu.vantix_hrm.dto.page.PageResponseDTO;
import poly.edu.vantix_hrm.entity.Department;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.exception.BusinessException;
import poly.edu.vantix_hrm.repository.DepartmentRepository;
import poly.edu.vantix_hrm.repository.EmployeeRepository;
import poly.edu.vantix_hrm.utils.BaseSpecification;
import poly.edu.vantix_hrm.utils.PageHelper;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    // ─── Lấy danh sách phân trang + lọc động ─────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponseDTO<DepartmentResponseDTO> getAll(String keyword, PageRequestDTO pageRequest) {
        Specification<Department> spec = Specification
                .where(BaseSpecification.<Department>search(keyword, "name", "description"))
                .and(BaseSpecification.isNotDeleted());

        return PageHelper.toResponse(
                departmentRepository.findAll(spec, PageHelper.createPageable(pageRequest))
                        .map(this::toResponse)
        );
    }

    // ─── Lấy chi tiết 1 department ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public DepartmentResponseDTO getById(Long id) {
        return toResponse(findById(id));
    }

    // ─── Tạo mới ────────────────────────────────────────────────────────────

    public DepartmentResponseDTO create(DepartmentRequestDTO request) {
        // Kiểm tra tên đã tồn tại
        if (departmentRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new BusinessException("name", "Tên phòng ban đã tồn tại!", HttpStatus.BAD_REQUEST);
        }

        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        // Gán trưởng phòng nếu có
        if (request.getManagerId() != null) {
            Employee manager = findEmployeeById(request.getManagerId());
            department.setManager(manager);
        }

        return toResponse(departmentRepository.save(department));
    }

    // ─── Cập nhật ───────────────────────────────────────────────────────────

    public DepartmentResponseDTO update(Long id, DepartmentRequestDTO request) {
        Department department = findById(id);

        // Kiểm tra tên trùng (bỏ qua chính nó)
        if (!department.getName().equals(request.getName()) &&
                departmentRepository.existsByNameAndIdNotAndDeletedFalse(request.getName(), id)) {
            throw new BusinessException("name", "Tên phòng ban đã tồn tại!", HttpStatus.BAD_REQUEST);
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());

        // Cập nhật trưởng phòng
        if (request.getManagerId() != null) {
            Employee manager = findEmployeeById(request.getManagerId());
            department.setManager(manager);
        } else {
            department.setManager(null);
        }

        return toResponse(departmentRepository.save(department));
    }

    // ─── Xóa mềm ────────────────────────────────────────────────────────────

    public void delete(Long id) {
        Department department = findById(id);

        // Kiểm tra có nhân viên không
        long employeeCount = departmentRepository.countEmployeesByDepartmentId(id);
        if (employeeCount > 0) {
            throw new BusinessException("id", "Không thể xóa phòng ban đang có nhân viên!", HttpStatus.BAD_REQUEST);
        }

        department.setDeleted(true);
        departmentRepository.save(department);
    }

    // ─── Helper ─────────────────────────────────────────────────────────────

    private Department findById(Long id) {
        return departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("id", "Không tìm thấy phòng ban!", HttpStatus.NOT_FOUND));
    }

    private Employee findEmployeeById(Long id) {
        return employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("managerId", "Không tìm thấy nhân viên!", HttpStatus.NOT_FOUND));
    }

    // ─── Map Entity → Response DTO ──────────────────────────────────────────

    private DepartmentResponseDTO toResponse(Department department) {
        return DepartmentResponseDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .managerId(department.getManager() != null ? department.getManager().getId() : null)
                .managerName(department.getManager() != null ? department.getManager().getFullName() : null)
                .employeeCount((int) departmentRepository.countEmployeesByDepartmentId(department.getId()))
                .positionCount((int) departmentRepository.countPositionsByDepartmentId(department.getId()))
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .createdBy(department.getCreatedBy())
                .updatedBy(department.getUpdatedBy())
                .build();
    }
}