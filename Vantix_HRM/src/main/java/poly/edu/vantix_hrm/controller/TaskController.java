package poly.edu.vantix_hrm.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.vantix_hrm.dto.task.TaskResponseDTO;
import poly.edu.vantix_hrm.entity.*;
import poly.edu.vantix_hrm.repository.*;
import poly.edu.vantix_hrm.service.TaskService;
import org.springframework.core.io.Resource;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskRepository taskRepo;
    private final TaskAssignmentRepository assignmentRepo;
    private final TaskReportRepository reportRepo;
    private final Cloudinary cloudinary;

    // 1. GET ALL (ADMIN)
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAll() {
        return ResponseEntity.ok(taskService.getAllTasksWithDetails());
    }

    // 2. TẠO TASK
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Task task) {
        try {
            return ResponseEntity.ok(taskService.createTask(task));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi tạo task: " + e.getMessage());
        }
    }

    // 3. CẬP NHẬT TASK
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Task taskDetails) {
        try {
            taskService.updateTask(id, taskDetails);
            return ResponseEntity.ok("Cập nhật thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    // 4. GIAO VIỆC
    @PostMapping("/assign")
    public ResponseEntity<?> assignTask(@RequestBody Map<String, Object> payload) {
        try {
            Long taskId = Long.valueOf(payload.get("taskId").toString());
            Long employeeId = Long.valueOf(payload.get("employeeId").toString());
            taskService.assignTask(taskId, employeeId);
            return ResponseEntity.ok("Giao việc thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi giao việc: " + e.getMessage());
        }
    }

    // 5. LẤY TASK THEO NHÂN VIÊN
    @GetMapping("/my")
    public ResponseEntity<List<TaskResponseDTO>> myTasks(@RequestParam("employeeId") Long employeeId) {
        return ResponseEntity.ok(taskService.getTasksByEmployeeId(employeeId));
    }

    // 6. NỘP BÁO CÁO (HÀM XỊN NHẤT XỬ LÝ 3 BẢNG)
    @PostMapping(value = "/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> report(
            @RequestParam("taskId") Long taskId,
            @RequestParam("employeeId") Long employeeId,
            @RequestParam(value = "workDescription", required = false) String workDescription,
            @RequestParam(value = "progressPercent", required = false) Integer progressPercent,
            @RequestParam(value = "status", required = false) String status,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            Task task = taskRepo.findById(taskId).orElseThrow(() -> new RuntimeException("Không tìm thấy Task"));

            if (file != null && !file.isEmpty()) {
                // 1. Lấy tên gốc của file (ví dụ: "bao_cao.pdf")
                String originalName = file.getOriginalFilename();

                // 2. Upload với cấu hình an toàn
                Map uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "resource_type", "auto",
                                "use_filename", true,       // Giữ lại tên gốc của người dùng
                                "unique_filename", true,    // 🔥 BẬT TRUE: Cloudinary sẽ tự thêm vài ký tự random (vd: bao_cao_xyz123.pdf) để chống ghi đè
                                "folder", "vantix_hrm/task_evidence" // 📁 Gom hết vào 1 thư mục trên Cloudinary cho gọn máy chủ
                        )
                );
                task.setFileUrl(uploadResult.get("secure_url").toString());
            }

            if (status != null && !status.isEmpty()) {
                task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
            }
            taskRepo.save(task);

            TaskAssignment assignment = assignmentRepo.findByTaskIdAndEmployeeId(taskId, employeeId);
            if (assignment != null) {
                if (progressPercent != null) assignment.setPercentComplete(progressPercent);
                if (status != null) assignment.setStatus(status);
                assignmentRepo.save(assignment);
            }

            if (workDescription != null || progressPercent != null) {
                TaskReport report = new TaskReport();
                report.setTaskId(taskId);
                report.setEmployeeId(employeeId);
                report.setWorkDescription(workDescription);
                report.setProgressPercent(progressPercent != null ? progressPercent : 0);
                report.setReportDate(LocalDate.now());
                report.setCreatedAt(LocalDateTime.now());
                reportRepo.save(report);
            }

            return ResponseEntity.ok("Báo cáo thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    // 7. PHÊ DUYỆT (Đồng bộ 2 bảng)
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveTask(@PathVariable Long id) {
        try {
            Task task = taskRepo.findById(id).orElseThrow();
            task.setStatus(TaskStatus.COMPLETED);
            taskRepo.save(task);

            if (task.getEmployeeId() != null) {
                TaskAssignment assignment = assignmentRepo.findByTaskIdAndEmployeeId(task.getTaskId(), task.getEmployeeId());
                if (assignment != null) {
                    assignment.setStatus("COMPLETED");
                    assignmentRepo.save(assignment);
                }
            }
            return ResponseEntity.ok("Phê duyệt thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    // 8. HỦY TASK (Đồng bộ 2 bảng)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelTask(@PathVariable Long id) {
        try {
            Task task = taskRepo.findById(id).orElseThrow();
            task.setStatus(TaskStatus.CANCELLED);
            taskRepo.save(task);

            if (task.getEmployeeId() != null) {
                TaskAssignment assignment = assignmentRepo.findByTaskIdAndEmployeeId(task.getTaskId(), task.getEmployeeId());
                if (assignment != null) {
                    assignment.setStatus("CANCELLED");
                    assignmentRepo.save(assignment);
                }
            }
            return ResponseEntity.ok("Đã hủy công việc!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    // 9. YÊU CẦU LÀM LẠI (Đồng bộ 2 bảng)
    @PutMapping("/{id}/reopen")
    public ResponseEntity<?> reopenTask(@PathVariable Long id) {
        try {
            Task task = taskRepo.findById(id).orElseThrow();
            task.setStatus(TaskStatus.IN_PROGRESS);
            taskRepo.save(task);

            if (task.getEmployeeId() != null) {
                TaskAssignment assignment = assignmentRepo.findByTaskIdAndEmployeeId(task.getTaskId(), task.getEmployeeId());
                if (assignment != null) {
                    assignment.setStatus("IN_PROGRESS");
                    // Không reset % để nhân viên sửa tiếp, nếu muốn reset thì thêm: assignment.setPercentComplete(0);
                    assignmentRepo.save(assignment);
                }
            }
            return ResponseEntity.ok("Đã trả lại task cho nhân viên!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    // 10. XẾP HẠNG (RANKING)
    @GetMapping("/ranking")
    public ResponseEntity<?> getRanking(@RequestParam(value = "month", required = false) Integer month) {
        try {
            List<Task> completedTasks = taskRepo.findAll().stream()
                    .filter(t -> t.getStatus() != null && t.getStatus() == TaskStatus.COMPLETED)
                    .filter(t -> month == null || month == 0 || (t.getCreatedAt() != null && t.getCreatedAt().getMonthValue() == month))
                    .toList();

            java.util.Map<Long, Integer> pointsMap = new java.util.HashMap<>();
            java.util.Map<Long, Integer> countMap = new java.util.HashMap<>();

            for (Task t : completedTasks) {
                if (t.getEmployeeId() != null) {
                    pointsMap.put(t.getEmployeeId(), pointsMap.getOrDefault(t.getEmployeeId(), 0) + (t.getPoint() != null ? t.getPoint() : 0));
                    countMap.put(t.getEmployeeId(), countMap.getOrDefault(t.getEmployeeId(), 0) + 1);
                }
            }

            List<Map<String, Object>> ranking = new java.util.ArrayList<>();
            for (Long empId : pointsMap.keySet()) {
                Map<String, Object> rank = new java.util.HashMap<>();
                rank.put("employeeId", empId);
                rank.put("totalPoints", pointsMap.get(empId));
                rank.put("completedTasks", countMap.get(empId));
                ranking.add(rank);
            }

            ranking.sort((a, b) -> ((Integer) b.get("totalPoints")).compareTo((Integer) a.get("totalPoints")));
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/download/{taskId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long taskId)
            throws Exception {

        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

        URL url = new URL(task.getFileUrl());

        InputStreamResource resource =
                new InputStreamResource(url.openStream());

        String fileName = "task-file";

        String[] parts = task.getFileUrl().split("/");
        if (parts.length > 0) {
            fileName = parts[parts.length - 1];
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .header(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(resource);
    }
}