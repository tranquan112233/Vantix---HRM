package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.employee.EmployeeRequest;
import poly.edu.vantix_hrm.dto.employee.EmployeeResponse;
import poly.edu.vantix_hrm.entity.*;
import poly.edu.vantix_hrm.exception.BusinessException;
import poly.edu.vantix_hrm.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /* ================= GET ALL ================= */

    public List<EmployeeResponse> getAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /* ================= GET BY ID ================= */

    public EmployeeResponse getById(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("employee", "Employee not found"));

        return mapToResponse(employee);
    }

    /* ================= CREATE ================= */

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {

        // Check email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("email", "Email already exists");
        }

        // Check username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("username", "Username already exists");
        }

        // Find role
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("role", "Role not found"));

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        // Auto set status theo workStatus
        if (request.getWorkStatus() == Employee.WorkStatus.RESIGNED) {
            user.setStatus(User.UserStatus.LOCKED);
        } else {
            user.setStatus(User.UserStatus.ACTIVE);
        }

        user = userRepository.save(user);

        // Find department & position
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException("department", "Department not found"));

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new BusinessException("position", "Position not found"));

        // Create employee
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setFullName(request.getFullName());
        employee.setGender(request.getGender());
        employee.setBirthDate(request.getBirthDate());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setWorkStatus(request.getWorkStatus());

        employee = employeeRepository.save(employee);

        return mapToResponse(employee);
    }

    /* ================= UPDATE ================= */

    @Transactional
    public EmployeeResponse update(Integer id, EmployeeRequest request) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("employee", "Employee not found"));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException("department", "Department not found"));

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new BusinessException("position", "Position not found"));

        employee.setFullName(request.getFullName());
        employee.setGender(request.getGender());
        employee.setBirthDate(request.getBirthDate());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setWorkStatus(request.getWorkStatus());

        // 🔥 Auto lock/unlock user theo workStatus
        if (request.getWorkStatus() == Employee.WorkStatus.RESIGNED) {
            employee.getUser().setStatus(User.UserStatus.LOCKED);
        } else {
            employee.getUser().setStatus(User.UserStatus.ACTIVE);
        }

        return mapToResponse(employeeRepository.save(employee));
    }

    /* ================= DELETE ================= */

    @Transactional
    public void delete(Integer id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("employee", "Employee not found"));

        // Xóa employee trước để tránh lỗi FK
        employeeRepository.delete(employee);

        // Sau đó xóa user
        userRepository.delete(employee.getUser());
    }

    /* ================= MAPPER ================= */

    private EmployeeResponse mapToResponse(Employee employee) {
        return EmployeeResponse.builder()
                .employeeId(employee.getEmployeeId())
                .fullName(employee.getFullName())
                .gender(employee.getGender())
                .birthDate(employee.getBirthDate())
                .phone(employee.getPhone())
                .address(employee.getAddress())
                .departmentId(employee.getDepartment().getDepartmentId())
                .departmentName(employee.getDepartment().getDepartmentName())
                .positionId(employee.getPosition().getPositionId())
                .positionName(employee.getPosition().getPositionName())
                .workStatus(employee.getWorkStatus())
                .build();
    }
}