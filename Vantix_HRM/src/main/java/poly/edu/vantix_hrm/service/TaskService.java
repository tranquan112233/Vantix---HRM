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

    // 1. LẤY TẤT CẢ TASK (Cho Admin)
    public List<TaskResponseDTO> getAllTasksWithDetails() {
        return taskRepo.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // 2. LẤY TASK THEO NHÂN VIÊN (Cho My Task)
    public List<TaskResponseDTO> getTasksByEmployeeId(Long employeeId) {
        return taskRepo.findTasksByEmployeeId(employeeId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // 3. TẠO TASK
    // 3. TẠO TASK (Đã bổ sung tự động tính điểm và ngày tháng)
    @Transactional
    public Task createTask(Task task) {
        // 1. Tự động set trạng thái mặc định
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.OPEN);
        }

        // 2. Tự động set thời gian tạo
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(LocalDateTime.now());
        }

        // 3. TỰ ĐỘNG TÍNH ĐIỂM (KPI) NẾU FRONTEND KHÔNG GỬI
        // Công thức ví dụ: (Độ khó + Khẩn cấp) * 10
        if (task.getPoint() == null) {
            int diff = task.getDifficultyLevel() != null ? task.getDifficultyLevel() : 1;
            int urg = task.getUrgencyLevel() != null ? task.getUrgencyLevel() : 1;
            task.setPoint((diff + urg) * 10);
        }

        return taskRepo.save(task);
    }

    // 4. CẬP NHẬT TASK
    @Transactional
    public Task updateTask(Long id, Task taskDetails) {
        Task task = taskRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Task"));
        task.setTaskTitle(taskDetails.getTaskTitle());
        task.setDescription(taskDetails.getDescription());
        task.setDifficultyLevel(taskDetails.getDifficultyLevel());
        task.setUrgencyLevel(taskDetails.getUrgencyLevel());
        return taskRepo.save(task);
    }

    // 5. GIAO VIỆC
    @Transactional
    public void assignTask(Long taskId, Long employeeId) {
        Task task = taskRepo.findById(taskId).orElseThrow(() -> new RuntimeException("Không tìm thấy Task"));
        Employee employee = employeeRepo.findById(employeeId).orElseThrow(() -> new RuntimeException("Không tìm thấy NV"));

        // Cập nhật bảng Task gốc
        task.setEmployeeId(employeeId);
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepo.save(task);

        // Lưu vào bảng phân công
        TaskAssignment assignment = new TaskAssignment();
        assignment.setTask(task);
        assignment.setEmployee(employee);
        assignment.setAssignedDate(LocalDateTime.now());
        assignment.setStatus("IN_PROGRESS");
        assignment.setPercentComplete(0);
        assignmentRepo.save(assignment);
    }

    // HÀM CHUYỂN ĐỔI DỮ LIỆU TỪ 3 BẢNG VÀO 1 DTO
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
            // Lấy tên nhân viên
            employeeRepo.findById(task.getEmployeeId()).ifPresent(emp -> dto.setEmployeeName(emp.getFullName()));

            // Lấy % tiến độ
            TaskAssignment assignment = assignmentRepo.findByTaskIdAndEmployeeId(task.getTaskId(), task.getEmployeeId());
            if (assignment != null) {
                dto.setProgressPercent(assignment.getPercentComplete() != null ? assignment.getPercentComplete() : 0);
            } else {
                dto.setProgressPercent(0);
            }
        }
        return dto;
    }
}