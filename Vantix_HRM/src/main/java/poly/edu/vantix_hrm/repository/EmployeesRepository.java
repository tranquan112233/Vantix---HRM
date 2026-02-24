package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Employee;

@Repository
public interface EmployeesRepository extends JpaRepository<Employee, Integer> {
    
}