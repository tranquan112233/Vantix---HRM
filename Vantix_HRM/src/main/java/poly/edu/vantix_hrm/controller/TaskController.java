package poly.edu.vantix_hrm.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.vantix_hrm.dto.task.TaskResponseDTO;
import poly.edu.vantix_hrm.entity.*;
import poly.edu.vantix_hrm.repository.*;
import poly.edu.vantix_hrm.service.TaskService;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskRepository taskRepo;
    private final TaskAssignmentRepository assignmentRepo;
    private final TaskReportRepository reportRepo;
    private final Cloudinary cloudinary;

    /* ================= GET ALL ================= */
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAll() {
        return ResponseEntity.ok(taskService.getAllTasksWithDetails());
    }

    /* ================= CREATE ================= */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Task task) {
        try {
            return ResponseEntity.ok(taskService.createTask(task));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    /* ================= UPDATE ================= */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Task taskDetails) {
        try {
            taskService.updateTask(id, taskDetails);
            return ResponseEntity.ok("Cập nhật thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    /* ================= ASSIGN ================= */
    @PostMapping("/assign")
    public ResponseEntity<?> assignTask(@RequestBody Map<String, Object> payload) {
        try {
            Long taskId = Long.valueOf(payload.get("taskId").toString());
            Long employeeId = Long.valueOf(payload.get("employeeId").toString());

            taskService.assignTask(taskId, employeeId);
            return ResponseEntity.ok("Giao việc thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    /* ================= MY TASK ================= */
    @GetMapping("/my")
    public ResponseEntity<List<TaskResponseDTO>> myTasks(
            @RequestParam Long employeeId) {

        return ResponseEntity.ok(
                taskService.getTasksByEmployeeId(employeeId)
        );
    }

    /* ================= REPORT + UPLOAD FILE ================= */
    @PostMapping(value = "/report",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> report(
            @RequestParam Long taskId,
            @RequestParam Long employeeId,
            @RequestParam(required = false) String workDescription,
            @RequestParam(required = false) Integer progressPercent,
            @RequestParam(required = false) String status,
            @RequestPart(required = false) MultipartFile file
    ) {
        try {

            Task task = taskRepo.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

            /* ===== UPLOAD CLOUDINARY ===== */
            if (file != null && !file.isEmpty()) {

                String originalName = file.getOriginalFilename();

                String fileNameWithoutExt = originalName;
                int dotIndex = originalName.lastIndexOf(".");
                if (dotIndex > 0) {
                    fileNameWithoutExt = originalName.substring(0, dotIndex);
                }

                // ✅ tạo file tạm
                File tempFile = File.createTempFile("upload_", originalName);

                file.transferTo(tempFile); // ghi multipart -> file thật

                Map uploadResult = cloudinary.uploader().upload(
                        tempFile,
                        ObjectUtils.asMap(
                                "resource_type", "auto",
                                "public_id", "vantix_hrm/task_evidence/" + fileNameWithoutExt,
                                "use_filename", true,
                                "unique_filename", false,
                                "overwrite", true
                        )
                );

                task.setFileUrl(uploadResult.get("secure_url").toString());
            }

            if (status != null) {
                task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
            }

            taskRepo.save(task);

            /* ===== UPDATE ASSIGNMENT ===== */
            TaskAssignment assignment =
                    assignmentRepo.findByTaskIdAndEmployeeId(taskId, employeeId);

            if (assignment != null) {
                if (progressPercent != null)
                    assignment.setPercentComplete(progressPercent);

                if (status != null)
                    assignment.setStatus(status);

                assignmentRepo.save(assignment);
            }

            /* ===== SAVE REPORT ===== */
            if (workDescription != null || progressPercent != null) {

                TaskReport report = new TaskReport();
                report.setTaskId(taskId);
                report.setEmployeeId(employeeId);
                report.setWorkDescription(workDescription);
                report.setProgressPercent(
                        progressPercent != null ? progressPercent : 0
                );
                report.setReportDate(LocalDate.now());
                report.setCreatedAt(LocalDateTime.now());

                reportRepo.save(report);
            }

            return ResponseEntity.ok("Báo cáo thành công");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    /* ================= APPROVE ================= */
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {

        Task task = taskRepo.findById(id).orElseThrow();
        task.setStatus(TaskStatus.COMPLETED);
        taskRepo.save(task);

        if (task.getEmployeeId() != null) {
            TaskAssignment a =
                    assignmentRepo.findByTaskIdAndEmployeeId(
                            task.getTaskId(),
                            task.getEmployeeId());

            if (a != null) {
                a.setStatus("COMPLETED");
                assignmentRepo.save(a);
            }
        }

        return ResponseEntity.ok("Phê duyệt thành công");
    }

    /* ================= CANCEL ================= */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {

        Task task = taskRepo.findById(id).orElseThrow();
        task.setStatus(TaskStatus.CANCELLED);
        taskRepo.save(task);

        return ResponseEntity.ok("Đã hủy task");
    }

    /* ================= REOPEN ================= */
    @PutMapping("/{id}/reopen")
    public ResponseEntity<?> reopen(@PathVariable Long id) {

        Task task = taskRepo.findById(id).orElseThrow();
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepo.save(task);

        return ResponseEntity.ok("Đã mở lại task");
    }

    /* ================= DOWNLOAD FILE ================= */
    @GetMapping("/download/{taskId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long taskId) throws Exception {

        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

        URL url = new URL(task.getFileUrl());
        var connection = url.openConnection();

        InputStreamResource resource =
                new InputStreamResource(connection.getInputStream());

        String contentType = connection.getContentType();

        String fileName = task.getFileUrl()
                .substring(task.getFileUrl().lastIndexOf("/") + 1);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}