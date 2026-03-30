package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Employee;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>,
        JpaSpecificationExecutor<Employee> {

    // Kiểm tra user_id đã có employee chưa
    boolean existsByUserIdAndDeletedFalse(Long userId);

    // Kiểm tra email đã tồn tại (qua user)
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Employee e " +
            "WHERE e.user.email = :email AND e.deleted = false")
    boolean existsByEmailAndDeletedFalse(@Param("email") String email);

    // Tìm employee chưa xóa
    Optional<Employee> findByIdAndDeletedFalse(Long id);

    // Tìm employee theo user_id
    Optional<Employee> findByUserIdAndDeletedFalse(Long userId);

    // Đếm số nhân viên theo phòng ban
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId AND e.deleted = false")
    long countByDepartmentId(@Param("departmentId") Long departmentId);

    // Đếm số nhân viên theo chức vụ
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.position.id = :positionId AND e.deleted = false")
    long countByPositionId(@Param("positionId") Long positionId);
}