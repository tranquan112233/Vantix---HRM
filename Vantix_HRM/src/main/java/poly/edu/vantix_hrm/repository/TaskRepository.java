package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Task;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Chỉ cần hàm này để phục vụ cho màn hình My Task của nhân viên
    List<Task> findTasksByEmployeeId(Long employeeId);

}