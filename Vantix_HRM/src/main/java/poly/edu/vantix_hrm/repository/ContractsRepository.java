package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Contract;

import java.util.List;

@Repository
public interface ContractsRepository extends JpaRepository<Contract, Integer> {

    @Query(value = "SELECT * FROM contract WHERE employee_id = :employeeId", nativeQuery = true)
    List<Contract> findByEmployee_EmployeeId(@Param("employeeId") Integer employeeId);
}
