package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Position;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long>,
        JpaSpecificationExecutor<Position> {

    // Kiểm tra tên đã tồn tại trong phòng ban (chưa xóa)
    boolean existsByNameAndDepartmentIdAndDeletedFalse(String name, Long departmentId);

    // Dùng khi update — bỏ qua chính nó
    boolean existsByNameAndDepartmentIdAndIdNotAndDeletedFalse(String name, Long departmentId, Long id);

    // Tìm position chưa xóa
    Optional<Position> findByIdAndDeletedFalse(Long id);

    // Lấy danh sách position theo department (chưa xóa)
    List<Position> findByDepartmentIdAndDeletedFalse(Long departmentId);

    // Đếm số nhân viên giữ vị trí này
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.position.id = :positionId AND e.deleted = false")
    long countEmployeesByPositionId(@Param("positionId") Long positionId);
}