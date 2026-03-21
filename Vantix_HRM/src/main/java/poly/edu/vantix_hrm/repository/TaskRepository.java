package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix_hrm.entity.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query("""
        SELECT t
        FROM Task t
        WHERE t.taskId IN (
            SELECT ta.taskId
            FROM TaskAssignment ta
            WHERE ta.employeeId = :employeeId
        )
    """)
    List<Task> findTasksByEmployeeId(@Param("employeeId") Integer employeeId);


    @Query(value = """
    SELECT 
        t.task_id,
        t.task_title,
        t.point,
        COALESCE(MAX(tr.progress_percent), 0) AS progress
    FROM Tasks t
    JOIN Task_Assignments ta ON t.task_id = ta.task_id
    LEFT JOIN Task_Reports tr 
        ON t.task_id = tr.task_id 
        AND ta.employee_id = tr.employee_id
    WHERE ta.employee_id = :employeeId
    GROUP BY t.task_id, t.task_title, t.point
""", nativeQuery = true)
    List<Object[]> getMyTasksWithProgress(@Param("employeeId") Integer employeeId);
}