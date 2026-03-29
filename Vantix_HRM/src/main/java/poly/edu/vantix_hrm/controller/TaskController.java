package poly.edu.vantix_hrm.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.vantix_hrm.entity.Task;
import poly.edu.vantix_hrm.entity.TaskAssignment;
import poly.edu.vantix_hrm.entity.TaskReport;
import poly.edu.vantix_hrm.repository.TaskAssignmentRepository;
import poly.edu.vantix_hrm.repository.TaskReportRepository;
import poly.edu.vantix_hrm.repository.TaskRepository;
import poly.edu.vantix_hrm.service.TaskService;
import poly.edu.vantix_hrm.dto.task.TaskResponseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TaskController {

    private final TaskService taskService;
    private final TaskAssignmentRepository assignmentRepo;
    private final TaskReportRepository reportRepo;
    private final TaskRepository taskRepo;
    private final Cloudinary cloudinary;


    @GetMapping
    public List<TaskResponseDTO> getAll() {
        // Gọi thẳng Repository để lấy danh sách Task kèm tên nhân viên
        return taskRepo.findAllWithEmployeeName();
    }

    // 🔥 API BÁO CÁO VÀ UPLOAD FILE (ĐÃ FIX LỖI GẠCH ĐỎ)
    // Trong TaskController.java
    @PostMapping("/report")
    public ResponseEntity<?> report(
            @RequestParam("taskId") Integer taskId,
            @RequestParam("employeeId") Integer employeeId,
            @RequestParam("workDescription") String workDescription,
            @RequestParam(value = "progressPercent", required = false) Integer progressPercent,
            @RequestParam(value = "status", required = false) String status, // "DONE" hoặc "IN_PROGRESS"
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        try {
            String fileUrl = null;
            if (file != null && !file.isEmpty()) {
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                        ObjectUtils.asMap(
                                "resource_type", "auto",
                                "type", "upload",
                                "use_filename", true,
                                "unique_filename", true
                        ));
                // Dùng secure_url để lấy link https cho chuẩn
                fileUrl = uploadResult.get("secure_url").toString();
            }

            // Tìm Task để cập nhật
            java.util.Optional<Task> taskOpt = taskRepo.findById(taskId);
            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();

                // 🔥 CẬP NHẬT TIẾN ĐỘ
                if (progressPercent != null) {
                    task.setProgressPercent(progressPercent);
                }

                // 🔥 CẬP NHẬT FILE URL
                if (fileUrl != null) {
                    task.setFileUrl(fileUrl);
                }

                // 🔥 QUAN TRỌNG NHẤT: Cập nhật Status để Vue có thể ẩn Task
                if (status != null && !status.isEmpty()) {
                    // Chuyển String "DONE" thành Enum TaskStatus.DONE
                    task.setStatus(poly.edu.vantix_hrm.entity.TaskStatus.valueOf(status.toUpperCase()));
                }

                taskRepo.save(task);

                // (Tùy chọn) Lưu vào bảng TaskReport để làm lịch sử báo cáo
                TaskReport report = new TaskReport();
                report.setTaskId(taskId);
                report.setEmployeeId(employeeId);
                report.setWorkDescription(workDescription);
                report.setFileUrl(fileUrl);
                report.setReportDate(LocalDateTime.now());
                reportRepo.save(report);
            }

            return ResponseEntity.ok("Báo cáo thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }


    @PostMapping
    public Task create(@RequestBody Task task) { return taskService.createTask(task); }

    @PutMapping("/{id}")
    public Task update(@PathVariable Integer id, @RequestBody Task taskDetails) {
        return taskService.updateTask(id, taskDetails);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) { taskService.deleteTask(id); }

    @GetMapping("/my")
    public List<Task> myTasks(@RequestParam("employeeId") Integer employeeId) {
        return taskService.getTaskByEmployee(employeeId);
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assignTask(@RequestBody Map<String, Object> payload) {
        try {
            Integer taskId = (Integer) payload.get("taskId");
            Integer employeeId = (Integer) payload.get("employeeId");

            // 1. Tạo bản ghi giao việc
            TaskAssignment assignment = new TaskAssignment();
            assignment.setTaskId(taskId);
            assignment.setEmployeeId(employeeId);
            assignment.setAssignedAt(LocalDateTime.now());
            assignmentRepo.save(assignment);

            // 2. Cập nhật lại bảng Task (để biết task này ai đang làm và đổi status)
            Task task = taskRepo.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Task ID: " + taskId));

            task.setEmployeeId(employeeId); // Bác nhớ thêm trường này vào Entity Task.java nhé
            task.setStatus(poly.edu.vantix_hrm.entity.TaskStatus.IN_PROGRESS);
            taskRepo.save(task);

            return ResponseEntity.ok("Giao việc thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi giao việc: " + e.getMessage());
        }
    }
}