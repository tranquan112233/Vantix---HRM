package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.dto.department.DepartmentRequest;
import poly.edu.vantix_hrm.dto.department.DepartmentResponse;
import poly.edu.vantix_hrm.entity.Department;
import poly.edu.vantix_hrm.exception.BusinessException;
import poly.edu.vantix_hrm.repository.DepartmentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRespository;

    // ================= FIND ALL =================
    public List<DepartmentResponse> findAll() {
        return departmentRespository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= FIND BY ID =================
    public DepartmentResponse findById(Integer id) {
        Department department = departmentRespository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("department", "Department not found"));

        return mapToResponse(department);
    }

    // ================= CREATE =================
    public DepartmentResponse create(DepartmentRequest request) {

        String departmentName = request.getDepartmentName().trim();

        if (departmentRespository.existsByDepartmentName(departmentName)) {
            throw new BusinessException("departmentName", "Department name already exists");
        }

        Department department = Department.builder()
                .departmentName(departmentName)
                .description(request.getDescription())
                .build();

        departmentRespository.save(department);

        return mapToResponse(department);
    }

    // ================= UPDATE =================
    public DepartmentResponse update(Integer id, DepartmentRequest request) {

        Department department = departmentRespository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("department", "Department not found"));

        String departmentName = request.getDepartmentName().trim();

        if (!department.getDepartmentName().equals(departmentName)
                && departmentRespository.existsByDepartmentName(departmentName)) {

            throw new BusinessException("departmentName", "Department name already exists");
        }

        department.setDepartmentName(departmentName);
        department.setDescription(request.getDescription());

        departmentRespository.save(department);

        return mapToResponse(department);
    }

    // ================= DELETE =================
    public void delete(Integer id) {
        Department department = departmentRespository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("department", "Department not found"));

        departmentRespository.delete(department);
    }

    // ================= MAP =================
    private DepartmentResponse mapToResponse(Department department) {
        return DepartmentResponse.builder()
                .departmentId(department.getDepartmentId())
                .departmentName(department.getDepartmentName())
                .description(department.getDescription())
                .build();
    }
}