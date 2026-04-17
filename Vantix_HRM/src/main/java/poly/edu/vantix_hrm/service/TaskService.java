package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.task.TaskResponseDTO;
import poly.edu.vantix_hrm.entity.*;
import poly.edu.vantix_hrm.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepo;
    private final TaskAssignmentRepository assignmentRepo;
    private final EmployeeRepository employeeRepo;
    private final NotificationService notificationService;

    /* =====================================================
       1. LẤY TẤT CẢ TASK (ADMIN)
    ===================================================== */
    public List<TaskResponseDTO> getAllTasksWithDetails() {
        return taskRepo.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /* =====================================================
       2. TASK THEO NHÂN VIÊN
    ===================================================== */
    public List<TaskResponseDTO> getTasksByEmployeeId(Long employeeId) {
        return taskRepo.findTasksByEmployeeId(employeeId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /* =====================================================
       3. TẠO TASK
    ===================================================== */
    @Transactional
    public Task createTask(Task task) {

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.OPEN);
        }

        if (task.getCreatedAt() == null) {
            task.setCreatedAt(LocalDateTime.now());
        }

        // Auto KPI point
        if (task.getPoint() == null) {
            int diff = task.getDifficultyLevel() != null ? task.getDifficultyLevel() : 1;
            int urg = task.getUrgencyLevel() != null ? task.getUrgencyLevel() : 1;
            task.setPoint((diff + urg) * 10);
        }

        return taskRepo.save(task);
    }

    /* =====================================================
       4. UPDATE TASK
    ===================================================== */
    @Transactional
    public Task updateTask(Long id, Task taskDetails) {

        Task task = taskRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Task"));

        task.setTaskTitle(taskDetails.getTaskTitle());
        task.setDescription(taskDetails.getDescription());
        task.setDifficultyLevel(taskDetails.getDifficultyLevel());
        task.setUrgencyLevel(taskDetails.getUrgencyLevel());

        // tính lại điểm khi sửa
        int diff = task.getDifficultyLevel() != null ? task.getDifficultyLevel() : 1;
        int urg = task.getUrgencyLevel() != null ? task.getUrgencyLevel() : 1;
        task.setPoint((diff + urg) * 10);

        return taskRepo.save(task);
    }

    /* =====================================================
       5. GIAO VIỆC
    ===================================================== */
    @Transactional
    public void assignTask(Long taskId, Long employeeId) {

        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Task"));

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        task.setEmployeeId(employeeId);
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepo.save(task);

        TaskAssignment assignment = new TaskAssignment();
        assignment.setTask(task);
        assignment.setEmployee(employee);
        assignment.setAssignedDate(LocalDateTime.now());
        assignment.setStatus("IN_PROGRESS");
        assignment.setPercentComplete(0);

        // Trong hàm assignTask
        assignmentRepo.save(assignment);
// CHÈN THÊM: Báo cho nhân viên có việc mới
        notificationService.sendNotification(employee.getUser().getId(), "📋 Công việc mới",
                "Bạn được giao nhiệm vụ: " + task.getTaskTitle(), "TASK", "/tasks");
    }

    /* =====================================================
       DTO MAPPING
    ===================================================== */
    private TaskResponseDTO mapToDTO(Task task) {

        TaskResponseDTO dto = new TaskResponseDTO();

        dto.setTaskId(task.getTaskId());
        dto.setTaskTitle(task.getTaskTitle());
        dto.setDescription(task.getDescription());
        dto.setDifficultyLevel(task.getDifficultyLevel());
        dto.setUrgencyLevel(task.getUrgencyLevel());
        dto.setPoint(task.getPoint());
        dto.setStatus(task.getStatus());
        dto.setFileUrl(task.getFileUrl());
        dto.setEmployeeId(task.getEmployeeId());
        dto.setCreatedAt(task.getCreatedAt());

        if (task.getEmployeeId() != null) {

            employeeRepo.findById(task.getEmployeeId())
                    .ifPresent(emp -> dto.setEmployeeName(emp.getFullName()));

            TaskAssignment assignment =
                    assignmentRepo.findByTaskIdAndEmployeeId(
                            task.getTaskId(),
                            task.getEmployeeId()
                    );

            dto.setProgressPercent(
                    assignment != null && assignment.getPercentComplete() != null
                            ? assignment.getPercentComplete()
                            : 0
            );
        }

        return dto;
    }
}