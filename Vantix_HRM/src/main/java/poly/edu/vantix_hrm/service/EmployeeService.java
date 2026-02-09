package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.dto.EmployeeRequest;
import poly.edu.vantix_hrm.dto.EmployeeResponse;
import poly.edu.vantix_hrm.dto.SimpleDepartmentDTO;
import poly.edu.vantix_hrm.dto.SimplePositionDTO;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.repository.DepartmentRepository;
import poly.edu.vantix_hrm.repository.EmployeeRepository;
import poly.edu.vantix_hrm.repository.PositionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EmployeeResponse create(EmployeeRequest req) {
        Employee e = new Employee();
        mapToEntity(e, req);
        return toResponse(employeeRepository.save(e));
    }

    public EmployeeResponse update(Integer id, EmployeeRequest req) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        mapToEntity(e, req);
        return toResponse(employeeRepository.save(e));
    }

    public void delete(Integer id) {
        employeeRepository.deleteById(id);
    }

    /* ================= MAP ================= */

    private void mapToEntity(Employee e, EmployeeRequest req) {
        e.setFullName(req.getFullName());
        e.setGender(req.getGender());
        e.setBirthDate(req.getBirthDate());
        e.setPhone(req.getPhone());
        e.setAddress(req.getAddress());
        e.setWorkStatus(req.getWorkStatus());

        e.setDepartment(
                departmentRepository.findById(req.getDepartmentId())
                        .orElseThrow(() -> new RuntimeException("Phòng ban không tồn tại"))
        );

        e.setPosition(
                positionRepository.findById(req.getPositionId())
                        .orElseThrow(() -> new RuntimeException("Chức vụ không tồn tại"))
        );
    }

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

