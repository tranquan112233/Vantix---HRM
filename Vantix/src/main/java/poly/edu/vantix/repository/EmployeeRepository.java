package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.enums.EmploymentStatus;
import poly.edu.vantix.entity.enums.Gender;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.position " +
           "LEFT JOIN FETCH e.user u " +
           "LEFT JOIN FETCH u.role " +
           "WHERE e.id = :id AND e.deleted = false")
    Optional<Employee> findActiveById(@Param("id") Long id);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department LEFT JOIN FETCH e.position " +
           "LEFT JOIN FETCH e.user u LEFT JOIN FETCH u.role " +
           "WHERE e.deleted = false AND e.user.id = :userId")
    Optional<Employee> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department LEFT JOIN FETCH e.position " +
           "LEFT JOIN FETCH e.user u LEFT JOIN FETCH u.role " +
           "WHERE e.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "AND (:gender IS NULL OR e.gender = :gender) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "ORDER BY e.id DESC")
    List<Employee> search(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("gender") Gender gender,
            @Param("status") EmploymentStatus status
    );

    @Query(
            value = "SELECT e FROM Employee e LEFT JOIN FETCH e.department LEFT JOIN FETCH e.position " +
                    "LEFT JOIN FETCH e.user u LEFT JOIN FETCH u.role " +
                    "WHERE e.deleted = false " +
                    "AND (:keyword IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                    "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
                    "AND (:gender IS NULL OR e.gender = :gender) " +
                    "AND (:status IS NULL OR e.status = :status) " +
                    "ORDER BY e.id DESC",
            countQuery = "SELECT COUNT(e) FROM Employee e " +
                    "WHERE e.deleted = false " +
                    "AND (:keyword IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                    "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
                    "AND (:gender IS NULL OR e.gender = :gender) " +
                    "AND (:status IS NULL OR e.status = :status)"
    )
    Page<Employee> search(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("gender") Gender gender,
            @Param("status") EmploymentStatus status,
            Pageable pageable
    );

    long countByDeletedFalse();

    long countByDeletedFalseAndStatus(EmploymentStatus status);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department " +
           "WHERE e.deleted = false ORDER BY e.createdAt DESC")
    List<Employee> findRecentEmployees();

    @Query("SELECT e FROM Employee e WHERE e.deleted = false " +
           "AND e.status IN (poly.edu.vantix.entity.enums.EmploymentStatus.ACTIVE, poly.edu.vantix.entity.enums.EmploymentStatus.PROBATION) " +
           "AND e.department.id = :departmentId " +
           "ORDER BY e.id ASC")
    List<Employee> findActiveByDepartmentId(@Param("departmentId") Long departmentId);

    boolean existsByEmployeeCodeAndDeletedFalse(String employeeCode);

    boolean existsByCitizenIdAndDeletedFalse(String citizenId);
}
