package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.vantix_hrm.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
