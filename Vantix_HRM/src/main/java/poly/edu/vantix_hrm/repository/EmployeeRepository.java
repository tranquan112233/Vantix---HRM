package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.vantix_hrm.entity.Employee;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {


    Optional <Employee> findByUser_Email(String email);

    Optional<Employee> findByUser_Username(String username);
}