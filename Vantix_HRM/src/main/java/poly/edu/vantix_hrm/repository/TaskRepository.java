package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Task;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Chỉ cần hàm này để phục vụ cho màn hình My Task của nhân viên
    List<Task> findTasksByEmployeeId(Long employeeId);

    @Query("""
            SELECT t.employeeId, COUNT(t), COALESCE(SUM(t.point), 0)
            FROM Task t
            WHERE t.employeeId IS NOT NULL
              AND t.status IN (poly.edu.vantix_hrm.entity.TaskStatus.DONE, poly.edu.vantix_hrm.entity.TaskStatus.COMPLETED)
              AND (:month IS NULL OR MONTH(t.createdAt) = :month)
            GROUP BY t.employeeId
            ORDER BY COALESCE(SUM(t.point), 0) DESC, COUNT(t) DESC
            """)
    List<Object[]> findKpiRanking(@Param("month") Integer month);

}
