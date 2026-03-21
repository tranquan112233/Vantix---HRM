package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.entity.Task;
import poly.edu.vantix_hrm.entity.TaskStatus;
import poly.edu.vantix_hrm.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    // 🔥 CREATE TASK (auto set point + status)
    public Task createTask(Task task) {

        // giả lập trưởng phòng (sau này lấy từ login)
        task.setCreatedBy(1);

        // set thời gian tạo
        task.setCreatedAt(LocalDateTime.now());

        // set status mặc định
        task.setStatus(TaskStatus.OPEN);

        // 🔥 tính point
        if (task.getDifficultyLevel() != null && task.getUrgencyLevel() != null) {
            int point = task.getDifficultyLevel() * task.getUrgencyLevel() * 10;
            task.setPoint(point);
        } else {
            task.setPoint(0);
        }

        return taskRepository.save(task);
    }

    // 🔥 GET ALL TASK
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // 🔥 GET TASK BY EMPLOYEE (đã fix method name)
    public List<Task> getTaskByEmployee(Integer employeeId) {
        return taskRepository.findTasksByEmployeeId(employeeId);
    }

}