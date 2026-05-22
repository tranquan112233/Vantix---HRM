package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.Payroll;

import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    @Query("SELECT p FROM Payroll p " +
           "LEFT JOIN FETCH p.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.position " +
           "LEFT JOIN FETCH p.period " +
           "LEFT JOIN FETCH p.contract " +
           "WHERE p.deleted = false AND p.id = :id")
    Optional<Payroll> findActiveById(@Param("id") Long id);

    @Query("SELECT p FROM Payroll p " +
           "LEFT JOIN FETCH p.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.position " +
           "LEFT JOIN FETCH p.period " +
           "LEFT JOIN FETCH p.contract " +
           "WHERE p.deleted = false AND p.period.id = :periodId " +
           "AND (:keyword IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "ORDER BY e.employeeCode ASC")
    List<Payroll> findByPeriod(
            @Param("periodId") Long periodId,
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId
    );

    @Query("SELECT p FROM Payroll p " +
           "LEFT JOIN FETCH p.period " +
           "LEFT JOIN FETCH p.contract " +
           "WHERE p.deleted = false AND p.employee.id = :employeeId " +
           "ORDER BY p.period.year DESC, p.period.month DESC")
    List<Payroll> findByEmployee(@Param("employeeId") Long employeeId);

    Optional<Payroll> findByPeriodIdAndEmployeeIdAndDeletedFalse(Long periodId, Long employeeId);

    Optional<Payroll> findByPeriodIdAndEmployeeId(Long periodId, Long employeeId);

    long countByPeriodIdAndDeletedFalse(Long periodId);

    long countByContractIdAndDeletedFalse(Long contractId);
}
