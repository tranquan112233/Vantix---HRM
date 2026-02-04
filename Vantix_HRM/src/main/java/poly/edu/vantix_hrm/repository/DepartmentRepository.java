package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.vantix_hrm.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
}
