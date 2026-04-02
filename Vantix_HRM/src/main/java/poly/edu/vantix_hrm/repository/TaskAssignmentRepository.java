package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.vantix_hrm.entity.TaskAssignment;
import java.util.List;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    // Khai báo hàm tìm kiếm theo ID nhân viên
    List<TaskAssignment> findByEmployee_Id(Long employeeId);
}