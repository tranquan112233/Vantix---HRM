package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.entity.Task;
import poly.edu.vantix_hrm.entity.TaskAssignment;
import poly.edu.vantix_hrm.entity.TaskStatus;
import poly.edu.vantix_hrm.repository.EmployeeRepository;
import poly.edu.vantix_hrm.repository.TaskAssignmentRepository;
import poly.edu.vantix_hrm.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepo;
    private final TaskAssignmentRepository assignmentRepo;
    private final EmployeeRepository employeeRepo;

    // 1. Lấy tất cả Task
    public List<Task> getAllTasks() {
        return taskRepo.findAll();
    }

    // 2. Tạo Task
    @Transactional
    public Task createTask(Task task) {
        return taskRepo.save(task);
    }

    // 3. Cập nhật Task (🔥 Đã thêm hàm này để Controller hết lỗi)
    @Transactional
    public Task updateTask(Long id, Task taskDetails) {
        Task task = taskRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Task"));
        task.setTaskTitle(taskDetails.getTaskTitle());
        task.setDescription(taskDetails.getDescription());
        task.setDifficultyLevel(taskDetails.getDifficultyLevel());
        task.setUrgencyLevel(taskDetails.getUrgencyLevel());
        return taskRepo.save(task);
    }

    // 4. Giao việc
    @Transactional
    public void assignTask(Long taskId, Long employeeId) {
        Task task = taskRepo.findById(taskId).orElseThrow(() -> new RuntimeException("Không tìm thấy Task"));
        Employee employee = employeeRepo.findById(employeeId).orElseThrow(() -> new RuntimeException("Không tìm thấy NV"));

        // Lưu bảng Assignment
        TaskAssignment assignment = new TaskAssignment();
        assignment.setTask(task);
        assignment.setEmployee(employee);
        assignment.setAssignedDate(LocalDateTime.now());
        assignment.setStatus("IN_PROGRESS");
        assignment.setPercentComplete(0);
        assignmentRepo.save(assignment);

        // Cập nhật bảng Task
        task.setEmployeeId(employeeId);
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepo.save(task);
    }

    // 5. Lấy Task theo nhân viên (🔥 Đã thêm hàm này để Controller hết lỗi)
    public List<Task> getTaskByEmployee(Long employeeId) {
        return taskRepo.findTasksByEmployeeId(employeeId);
    }

}