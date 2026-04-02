package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix_hrm.dto.task.TaskResponseDTO;
import poly.edu.vantix_hrm.entity.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> { // Đổi thành Long

    @Query("""
        SELECT t
        FROM Task t
        WHERE t.taskId IN (
            SELECT ta.task.taskId
            FROM TaskAssignment ta
            WHERE ta.employee.id = :employeeId
        )
    """)
    List<Task> findTasksByEmployeeId(@Param("employeeId") Long employeeId); // Đổi thành Long

    @Query(value = """
    SELECT 
        t.task_id as taskId, 
        t.task_title as taskTitle, 
        t.description as description,
        t.difficulty_level as difficultyLevel,
        t.point as point,
        t.status as status,
        COALESCE(MAX(tr.progress_percent), 0) as progressPercent
    FROM task t
    INNER JOIN task_assignment ta ON t.task_id = ta.task_id
    LEFT JOIN task_reports tr ON t.task_id = tr.task_id
    WHERE ta.employee_id = :employeeId
    GROUP BY t.task_id, t.task_title, t.description, t.difficulty_level, t.point, t.status
""", nativeQuery = true)
    List<MyTaskProjection> getMyTasksWithProgress(@Param("employeeId") Long employeeId);

    @Query("SELECT new poly.edu.vantix_hrm.dto.task.TaskResponseDTO(" +
            "t.taskId, t.taskTitle, t.description, t.difficultyLevel, t.urgencyLevel, " +
            "t.point, t.status, t.fileUrl, t.employeeId, e.fullName) " +
            "FROM Task t LEFT JOIN Employee e ON t.employeeId = e.id") // 🔥 Sửa e.employeeId thành e.id
    List<TaskResponseDTO> findAllWithEmployeeName();
}