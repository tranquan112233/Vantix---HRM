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

    // Lấy chi tiết 1 task
    public Task getTaskById(Integer id) {
        return taskRepository.findById(id).orElse(null);
    }

    // 🔥 CREATE TASK: Rút gọn, để Entity tự tính Point
    public Task createTask(Task task) {
        task.setCreatedBy(1); // Sau này thay bằng ID người dùng đăng nhập
        task.setCreatedAt(LocalDateTime.now());
        task.setStatus(TaskStatus.OPEN);

        // Không cần tính point ở đây vì @PrePersist trong Task.java đã lo rồi
        return taskRepository.save(task);
    }

    // 🔥 UPDATE TASK: Cập nhật mọi thông tin bao gồm cả link file
    public Task updateTask(Integer id, Task details) {
        return taskRepository.findById(id).map(task -> {
            task.setTaskTitle(details.getTaskTitle());
            task.setDescription(details.getDescription());
            task.setDifficultyLevel(details.getDifficultyLevel());
            task.setUrgencyLevel(details.getUrgencyLevel());
            task.setStatus(details.getStatus());
            task.setFileUrl(details.getFileUrl()); // Quan trọng để Admin sửa file nếu cần

            // JPA sẽ tự động gọi @PreUpdate để tính lại point nếu difficulty/urgency thay đổi
            return taskRepository.save(task);
        }).orElse(null);
    }

    public void deleteTask(Integer id) {
        taskRepository.deleteById(id);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTaskByEmployee(Integer employeeId) {
        // Hãy đảm bảo Repo đã có method này và Query đúng
        return taskRepository.findTasksByEmployeeId(employeeId);
    }
}