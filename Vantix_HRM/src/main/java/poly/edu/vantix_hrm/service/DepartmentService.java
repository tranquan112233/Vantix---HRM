package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.entity.Department;
import poly.edu.vantix_hrm.repository.DepartmentRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Optional<Department> findById(Integer id) {
        return departmentRepository.findById(id);
    }

    public Department create(Department department) {
        department.setId(null);
        return departmentRepository.save(department);
    }

    public Department update(Integer id, Department department) {
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        existing.setName(department.getName());
        existing.setDescription(department.getDescription());
        return departmentRepository.save(existing);
    }

    public void delete(Integer id) {
        if (!departmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Department not found");
        }
        departmentRepository.deleteById(id);
    }
}
