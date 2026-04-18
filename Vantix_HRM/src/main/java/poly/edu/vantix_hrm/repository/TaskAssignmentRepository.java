package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.TaskAssignment;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    // Thêm @Query để ép Spring Boot hiểu đúng ý mình, tránh bị nhầm tên cột
    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.task.taskId = :taskId AND ta.employee.id = :employeeId")
    TaskAssignment findByTaskIdAndEmployeeId(@Param("taskId") Long taskId, @Param("employeeId") Long employeeId);

    void deleteByTask_TaskId(Long taskId);

}
