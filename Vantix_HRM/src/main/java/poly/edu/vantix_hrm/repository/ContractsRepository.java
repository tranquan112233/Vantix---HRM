package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Contract;

import java.util.List;

@Repository
public interface ContractsRepository extends JpaRepository<Contract, Integer> {
    List<Contract> findByEmployee_EmployeeId(Integer employeeId);
}