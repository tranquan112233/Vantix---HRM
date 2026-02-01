package poly.edu.vantix_hrm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.vantix_hrm.entity.Departments;

public interface DepartmentDAO extends JpaRepository<Departments, Integer> {
}
