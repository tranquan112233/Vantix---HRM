package poly.edu.vantix_hrm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Employee;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>,
        JpaSpecificationExecutor<Employee> {

    // Tìm employee với tất cả associations được fetch JOIN để tránh N+1
    @EntityGraph(attributePaths = {"user", "department", "position", "department.manager"})
    Optional<Employee> findByIdAndDeletedFalse(Long id);

    // Tìm danh sách employee với fetch joins
    @EntityGraph(attributePaths = {"user", "department", "position", "department.manager"})
    Page<Employee> findAll(Specification<Employee> spec, Pageable pageable);

    Optional <Employee> findByUser_Email(String email);
    Optional<Employee> findByUser_Username(String username);

    // Kiểm tra user_id đã có employee chưa
    boolean existsByUserIdAndDeletedFalse(Long userId);

    // Kiểm tra email đã tồn tại (qua user)
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Employee e " +
            "WHERE e.user.email = :email AND e.deleted = false")
    boolean existsByEmailAndDeletedFalse(@Param("email") String email);

    // Tìm employee theo user_id
    @EntityGraph(attributePaths = {"user", "department", "position", "department.manager"})
    Optional<Employee> findByUserIdAndDeletedFalse(Long userId);

    // Đếm số nhân viên theo phòng ban
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId AND e.deleted = false")
    long countByDepartmentId(@Param("departmentId") Long departmentId);

    // Đếm số nhân viên theo chức vụ
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.position.id = :positionId AND e.deleted = false")
    long countByPositionId(@Param("positionId") Long positionId);

    List<Employee> findByDepartment_Id(Long departmentId);
    // Lấy tất cả nhân viên, sắp xếp theo tên
    List<Employee> findAllByOrderByFullNameAsc();

}