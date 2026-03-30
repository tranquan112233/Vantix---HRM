package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.employee.EmployeeRequestDTO;
import poly.edu.vantix_hrm.dto.employee.EmployeeResponseDTO;
import poly.edu.vantix_hrm.dto.page.PageRequestDTO;
import poly.edu.vantix_hrm.dto.page.PageResponseDTO;
import poly.edu.vantix_hrm.entity.*;
import poly.edu.vantix_hrm.exception.BusinessException;
import poly.edu.vantix_hrm.repository.*;
import poly.edu.vantix_hrm.utils.BaseSpecification;
import poly.edu.vantix_hrm.utils.PageHelper;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Lấy danh sách phân trang + lọc động ─────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponseDTO<EmployeeResponseDTO> getAll(String keyword, Employee.WorkStatus workStatus, Long departmentId, PageRequestDTO pageRequest) {
        Specification<Employee> spec = Specification
                .where(BaseSpecification.<Employee>search(keyword, "fullName", "user.email", "user.username"))
                .and(BaseSpecification.equal("workStatus", workStatus))
                .and(BaseSpecification.equal("department.id", departmentId))
                .and(BaseSpecification.isNotDeleted());

        return PageHelper.toResponse(
                employeeRepository.findAll(spec, PageHelper.createPageable(pageRequest))
                        .map(this::toResponse)
        );
    }

    // ─── Lấy chi tiết 1 employee ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public EmployeeResponseDTO getById(Long id) {
        return toResponse(findById(id));
    }

    // ─── Lấy employee theo user_id ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public EmployeeResponseDTO getByUserId(Long userId) {
        Employee employee = employeeRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException("userId", "Không tìm thấy nhân viên!", HttpStatus.NOT_FOUND));
        return toResponse(employee);
    }

    // ─── Tạo mới ────────────────────────────────────────────────────────────

    public EmployeeResponseDTO create(EmployeeRequestDTO request) {
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
            throw new BusinessException("username", "Username đã tồn tại!", HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new BusinessException("email", "Email đã tồn tại!", HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra user_id đã có employee chưa (nếu có)
        // Với create thì chưa có user, nên bỏ qua

        // Tạo User mới
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(findRoleById(request.getRoleId()))
                .status(User.UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        // Tạo Employee
        Department department = findDepartmentById(request.getDepartmentId());
        Position position = findPositionById(request.getPositionId());

        Employee employee = Employee.builder()
                .user(savedUser)
                .fullName(request.getFullName())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .phone(request.getPhone())
                .address(request.getAddress())
                .department(department)
                .position(position)
                .workStatus(request.getWorkStatus() != null ? request.getWorkStatus() : Employee.WorkStatus.WORKING)
                .build();

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    // ─── Cập nhật ───────────────────────────────────────────────────────────

    public EmployeeResponseDTO update(Long id, EmployeeRequestDTO request) {
        Employee employee = findById(id);
        User user = employee.getUser();

        // Kiểm tra username trùng (bỏ qua chính nó)
        if (!user.getUsername().equals(request.getUsername()) &&
                userRepository.existsByUsernameAndIdNotAndDeletedFalse(request.getUsername(), user.getId())) {
            throw new BusinessException("username", "Username đã tồn tại!", HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra email trùng (bỏ qua chính nó)
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmailAndIdNotAndDeletedFalse(request.getEmail(), user.getId())) {
            throw new BusinessException("email", "Email đã tồn tại!", HttpStatus.BAD_REQUEST);
        }

        // Cập nhật User
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(findRoleById(request.getRoleId()));

        // Cập nhật password nếu có
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        // Cập nhật Employee
        Department department = findDepartmentById(request.getDepartmentId());
        Position position = findPositionById(request.getPositionId());

        employee.setFullName(request.getFullName());
        employee.setGender(request.getGender());
        employee.setBirthDate(request.getBirthDate());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setWorkStatus(request.getWorkStatus() != null ? request.getWorkStatus() : employee.getWorkStatus());

        Employee updated = employeeRepository.save(employee);
        return toResponse(updated);
    }

    // ─── Xóa mềm ────────────────────────────────────────────────────────────

    public void delete(Long id) {
        Employee employee = findById(id);
        employee.setDeleted(true);

        // Cũng xóa mềm user liên quan
        User user = employee.getUser();
        user.setDeleted(true);

        employeeRepository.save(employee);
        userRepository.save(user);
    }

    // ─── Helper ─────────────────────────────────────────────────────────────

    private Employee findById(Long id) {
        return employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("id", "Không tìm thấy nhân viên!", HttpStatus.NOT_FOUND));
    }

    private Role findRoleById(Long roleId) {
        if (roleId == null) return null;
        return roleRepository.findById(roleId)
                .filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
                .orElseThrow(() -> new BusinessException("roleId", "Không tìm thấy vai trò!", HttpStatus.NOT_FOUND));
    }

    private Department findDepartmentById(Long id) {
        return departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("departmentId", "Không tìm thấy phòng ban!", HttpStatus.NOT_FOUND));
    }

    private Position findPositionById(Long id) {
        return positionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("positionId", "Không tìm thấy chức vụ!", HttpStatus.NOT_FOUND));
    }

    // ─── Map Entity → Response DTO ──────────────────────────────────────────

    private EmployeeResponseDTO toResponse(Employee employee) {
        User user = employee.getUser();
        Department department = employee.getDepartment();
        Position position = employee.getPosition();

        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .userStatus(user.getStatus())
                .lastActive(user.getLastActive())
                .fullName(employee.getFullName())
                .gender(employee.getGender())
                .birthDate(employee.getBirthDate())
                .phone(employee.getPhone())
                .address(employee.getAddress())
                .workStatus(employee.getWorkStatus())
                .departmentId(department != null ? department.getId() : null)
                .departmentName(department != null ? department.getName() : null)
                .managerId(department != null && department.getManager() != null ? department.getManager().getId() : null)
                .managerName(department != null && department.getManager() != null ? department.getManager().getFullName() : null)
                .positionId(position != null ? position.getId() : null)
                .positionName(position != null ? position.getName() : null)
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .createdBy(employee.getCreatedBy())
                .updatedBy(employee.getUpdatedBy())
                .build();
    }

    // Trong EmployeeService.java
    public boolean isValidEmployee(Long employeeId) {
        if (employeeId == null) return false;
        return employeeRepository.existsById(employeeId);
    }

    public Employee getValidEmployee(Long employeeId) {
        if (employeeId == null) {
            throw new BusinessException("employeeId", "Mã nhân viên không được để trống!", HttpStatus.BAD_REQUEST);
        }
        return findById(employeeId);
    }
}