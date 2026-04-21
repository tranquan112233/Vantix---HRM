package poly.edu.vantix.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.DepartmentRequest;
import poly.edu.vantix.dto.response.DepartmentResponse;
import poly.edu.vantix.dto.response.PageResponse;
import poly.edu.vantix.entity.Department;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.enums.EmploymentStatus;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.repository.DepartmentRepository;
import poly.edu.vantix.repository.EmployeeRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository
    ) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    // Danh sách phòng ban với tìm kiếm
    @Transactional(readOnly = true)
    public List<DepartmentResponse> search(String keyword) {
        return departmentRepository.search(keyword)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> searchPage(String keyword, Pageable pageable) {
        return PageResponse.from(departmentRepository.search(keyword, pageable), this::toResponse);
    }

    // Lấy chi tiết phòng ban
    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        Department department = findActiveById(id);
        return toResponse(department);
    }

    // Tạo mới phòng ban
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByCodeAndDeletedFalse(request.getCode())) {
            throw new BusinessException("code", "Department code already exists");
        }
        if (departmentRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new BusinessException("name", "Department name already exists");
        }

        Department department = new Department();
        mapRequestToEntity(request, department);

        department = departmentRepository.save(department);
        return toResponse(department);
    }

    // Cập nhật phòng ban
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = findActiveById(id);

        // Kiểm tra trùng code (trừ chính nó)
        departmentRepository.findByCode(request.getCode())
                .filter(d -> !d.getId().equals(id) && !d.getDeleted())
                .ifPresent(d -> {
                    throw new BusinessException("code", "Department code already exists");
                });

        mapRequestToEntity(request, department);
        department = departmentRepository.save(department);
        return toResponse(department);
    }

    // Xóa mềm phòng ban
    @Transactional
    public void delete(Long id) {
        Department department = findActiveById(id);
        department.setDeleted(true);
        department.setDeletedAt(LocalDateTime.now());
        departmentRepository.save(department);
    }

    // --- Helper ---

    private Department findActiveById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Department not found with id: " + id));
        if (department.getDeleted()) {
            throw new BusinessException("Department has been deleted");
        }
        return department;
    }

    private void mapRequestToEntity(DepartmentRequest request, Department department) {
        department.setCode(request.getCode());
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setHeadEmployeeId(resolveHeadEmployeeId(request.getHeadEmployeeId()));
    }

    private Long resolveHeadEmployeeId(Long headEmployeeId) {
        if (headEmployeeId == null) {
            return null;
        }

        Employee headEmployee = employeeRepository.findActiveById(headEmployeeId)
                .orElseThrow(() -> new BusinessException("headEmployeeId", "Head employee does not exist"));

        if (headEmployee.getStatus() == EmploymentStatus.RESIGNED
                || headEmployee.getStatus() == EmploymentStatus.TERMINATED) {
            throw new BusinessException("headEmployeeId", "Head employee must be an active employee");
        }

        return headEmployee.getId();
    }

    private DepartmentResponse toResponse(Department department) {
        Employee headEmployee = department.getHeadEmployeeId() == null
                ? null
                : employeeRepository.findActiveById(department.getHeadEmployeeId()).orElse(null);
        return DepartmentResponse.fromEntity(department, headEmployee);
    }
}
