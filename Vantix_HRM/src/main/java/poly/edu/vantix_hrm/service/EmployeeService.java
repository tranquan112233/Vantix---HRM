package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.EmployeeRequest;
import poly.edu.vantix_hrm.dto.EmployeeResponse;
import poly.edu.vantix_hrm.dto.SimpleDepartmentDTO;
import poly.edu.vantix_hrm.dto.SimplePositionDTO;
import poly.edu.vantix_hrm.entity.Department;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.entity.Position;
import poly.edu.vantix_hrm.entity.Role;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.repository.DepartmentRepository;
import poly.edu.vantix_hrm.repository.EmployeeRepository;
import poly.edu.vantix_hrm.repository.PositionRepository;
import poly.edu.vantix_hrm.repository.RoleRepository;
import poly.edu.vantix_hrm.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;


    /* ================= FIND ================= */

    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    /* ================= CREATE ================= */

    @Transactional
    public EmployeeResponse create(EmployeeRequest req) {

        // kiểm tra username tồn tại
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // tìm role
        Role role = roleRepository.findById(req.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));

        // tạo user
        User user = new User();

        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(role);

        userRepository.save(user);


        // tạo employee
        Employee employee = new Employee();

        mapToEntity(employee, req);

        // liên kết user
        employee.setUser(user);

        return toResponse(employeeRepository.save(employee));
    }


    /* ================= UPDATE ================= */

    @Transactional
    public EmployeeResponse update(Integer id, EmployeeRequest req) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // chỉ update employee, không update user
        mapToEntity(employee, req);

        return toResponse(employeeRepository.save(employee));
    }


    /* ================= DELETE ================= */

    @Transactional
    public void delete(Integer id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // xóa user trước
        if (employee.getUser() != null) {
            userRepository.delete(employee.getUser());
        }

        employeeRepository.delete(employee);
    }


    /* ================= MAP ================= */

    private void mapToEntity(Employee e, EmployeeRequest req) {

        e.setFullName(req.getFullName());
        e.setGender(req.getGender());
        e.setBirthDate(req.getBirthDate());
        e.setPhone(req.getPhone());
        e.setAddress(req.getAddress());
        e.setWorkStatus(req.getWorkStatus());

        Department department = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Phòng ban không tồn tại"));

        Position position = positionRepository.findById(req.getPositionId())
                .orElseThrow(() -> new RuntimeException("Chức vụ không tồn tại"));

        e.setDepartment(department);
        e.setPosition(position);
    }


    /* ================= RESPONSE ================= */

    private EmployeeResponse toResponse(Employee e) {

        EmployeeResponse dto = new EmployeeResponse();

        dto.setId(e.getId());
        dto.setFullName(e.getFullName());
        dto.setGender(e.getGender());
        dto.setBirthDate(e.getBirthDate());
        dto.setPhone(e.getPhone());
        dto.setAddress(e.getAddress());
        dto.setWorkStatus(e.getWorkStatus());

        if (e.getDepartment() != null) {
            SimpleDepartmentDTO d = new SimpleDepartmentDTO();
            d.setId(e.getDepartment().getId());
            d.setName(e.getDepartment().getName());
            dto.setDepartment(d);
        }

        if (e.getPosition() != null) {
            SimplePositionDTO p = new SimplePositionDTO();
            p.setId(e.getPosition().getId());
            p.setName(e.getPosition().getName());
            dto.setPosition(p);
        }

        return dto;
    }
}