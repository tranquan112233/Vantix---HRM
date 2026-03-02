package poly.edu.vantix_hrm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.dto.employee.EmployeeCreateRequest;
import poly.edu.vantix_hrm.dto.employee.EmployeeResponse;
import poly.edu.vantix_hrm.dto.employee.EmployeeUpdateRequest;
import poly.edu.vantix_hrm.entity.*;
import poly.edu.vantix_hrm.exception.BusinessException;
import poly.edu.vantix_hrm.repository.*;

import java.util.List;

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

    /* ================= FIND ================= */

    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public EmployeeResponse findById(Integer id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("employee","Employee not found"));

        return mapToResponse(employee);
    }

    /* ================= CREATE ================= */

    public EmployeeResponse create(EmployeeCreateRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("username","Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("email","Email already exists");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("role","Role not found"));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException("department","Department not found"));

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new BusinessException("position","Position not found"));

        /* CREATE USER */
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);

        userRepository.save(user);

        /* CREATE EMPLOYEE */
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setFullName(request.getFullName().trim());
        employee.setGender(request.getGender());
        employee.setBirthDate(request.getBirthDate());
        employee.setPhone(request.getPhone().trim());
        employee.setAddress(request.getAddress().trim());
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setWorkStatus(
                request.getWorkStatus() != null
                        ? request.getWorkStatus()
                        : Employee.WorkStatus.WORKING
        );

        employeeRepository.save(employee);

        return mapToResponse(employee);
    }

    /* ================= UPDATE ================= */

    public EmployeeResponse update(Integer id, EmployeeUpdateRequest request) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("employee","Employee not found"));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException("department","Department not found"));

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new BusinessException("position","Position not found"));

        employee.setFullName(request.getFullName().trim());
        employee.setGender(request.getGender());
        employee.setBirthDate(request.getBirthDate());
        employee.setPhone(request.getPhone().trim());
        employee.setAddress(request.getAddress().trim());
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setWorkStatus(request.getWorkStatus());

        employeeRepository.save(employee);

        return mapToResponse(employee);
    }

    /* ================= DELETE ================= */

    public void delete(Integer id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("employee","Employee not found"));

        employeeRepository.delete(employee);
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