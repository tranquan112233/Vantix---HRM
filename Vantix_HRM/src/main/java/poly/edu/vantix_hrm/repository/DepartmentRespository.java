package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Department;

import java.util.Optional;

@Repository
public interface DepartmentRespository extends JpaRepository<Department, Integer> {

    Optional<Department> findByDepartmentName(String departmentName);
}
