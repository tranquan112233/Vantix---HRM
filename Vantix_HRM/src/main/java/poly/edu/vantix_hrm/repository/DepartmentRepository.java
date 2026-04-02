package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Department;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>,
        JpaSpecificationExecutor<Department> {

    // Kiểm tra tên đã tồn tại (chưa xóa)
    boolean existsByNameAndDeletedFalse(String name);

    // Dùng khi update — bỏ qua chính nó
    boolean existsByNameAndIdNotAndDeletedFalse(String name, Long id);

    // Tìm department chưa xóa
    Optional<Department> findByIdAndDeletedFalse(Long id);

    // Đếm số nhân viên trong phòng ban
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId AND e.deleted = false")
    long countEmployeesByDepartmentId(@Param("departmentId") Long departmentId);

    // Đếm số vị trí trong phòng ban
    @Query("SELECT COUNT(p) FROM Position p WHERE p.department.id = :departmentId AND p.deleted = false")
    long countPositionsByDepartmentId(@Param("departmentId") Long departmentId);

}