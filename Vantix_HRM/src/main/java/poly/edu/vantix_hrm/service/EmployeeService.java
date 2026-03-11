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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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

        List<Role> roles = roleRepository.findAllById(request.getRoleIds());
        if (roles.isEmpty() || roles.size() != request.getRoleIds().size()) {
            throw new BusinessException("role", "One or more roles not found or invalid");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException("department","Department not found"));

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new BusinessException("position","Position not found"));

        /* 1. CREATE USER */
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(new HashSet<>(roles));
        user.setStatus(User.UserStatus.ACTIVE);

        // Nối mảng quyền thành chuỗi lưu vào Cột Permissions của USER
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            user.setPermissions(String.join(",", request.getPermissions()));
        } else {
            user.setPermissions("");
        }

        userRepository.save(user);

        /* 2. CREATE EMPLOYEE */
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

        List<Role> roles = roleRepository.findAllById(request.getRoleIds());
        if (roles.isEmpty() || roles.size() != request.getRoleIds().size()) {
            throw new BusinessException("role", "One or more roles not found or invalid");
        }

        /* 1. UPDATE USER */
        User user = employee.getUser();
        user.setEmail(request.getEmail().trim());
        user.setRoles(new HashSet<>(roles));

        // Cập nhật lại chuỗi Permissions cho USER
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            user.setPermissions(String.join(",", request.getPermissions()));
        } else {
            user.setPermissions("");
        }

        userRepository.save(user);

        /* 2. UPDATE EMPLOYEE */
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

        userRepository.delete(employee.getUser());
        employeeRepository.delete(employee);
    }

    /* ================= MAPPER ================= */

    private EmployeeResponse mapToResponse(Employee employee) {

        // Lấy danh sách RoleIds
        List<Integer> roleIds = new ArrayList<>();
        if (employee.getUser() != null && employee.getUser().getRoles() != null) {
            roleIds = employee.getUser().getRoles().stream()
                    .map(Role::getRoleId)
                    .collect(Collectors.toList());
        }

        // Cắt chuỗi quyền (permissions) từ DB thành mảng List<String> cho Vue.js đọc
        List<String> permissionList = new ArrayList<>();
        if (employee.getUser() != null && employee.getUser().getPermissions() != null
                && !employee.getUser().getPermissions().trim().isEmpty()) {
            permissionList = Arrays.asList(employee.getUser().getPermissions().split(","));
        }

        return EmployeeResponse.builder()
                .employeeId(employee.getEmployeeId())
                // TRẢ VỀ DỮ LIỆU USER ĐỂ FORM VUE CÓ CÁI HIỂN THỊ
                .username(employee.getUser() != null ? employee.getUser().getUsername() : null)
                .email(employee.getUser() != null ? employee.getUser().getEmail() : null)
                .roleIds(roleIds)
                .permissions(permissionList) // 🔥 Ném mảng quyền lên cho Frontend đây
                // DỮ LIỆU EMPLOYEE
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

    public Employee isEmployeeValid(Integer employeeId) {
        String msgError = "Không tìm thấy nhân viên (" + employeeId + ") trên hệ thống.";
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new RuntimeException(msgError));
        return employee;
    }
}