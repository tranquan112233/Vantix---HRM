package poly.edu.vantix_hrm.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.vantix_hrm.entity.Task;
import poly.edu.vantix_hrm.entity.TaskStatus;
import poly.edu.vantix_hrm.repository.TaskRepository;
import poly.edu.vantix_hrm.service.TaskService;
import poly.edu.vantix_hrm.dto.task.TaskResponseDTO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskRepository taskRepo;
    private final Cloudinary cloudinary;

    @GetMapping
    public List<TaskResponseDTO> getAll() {
        return taskRepo.findAllWithEmployeeName();
    }

    @PostMapping
    public Task create(@RequestBody Task task) {
        return taskService.createTask(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Task taskDetails) {
        try {
            taskService.updateTask(id, taskDetails);
            return ResponseEntity.ok("Cập nhật thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assignTask(@RequestBody Map<String, Object> payload) {
        try {
            Long taskId = Long.valueOf(payload.get("taskId").toString());
            Long employeeId = Long.valueOf(payload.get("employeeId").toString());

            // Gọi Service xử lý
            taskService.assignTask(taskId, employeeId);
            return ResponseEntity.ok("Giao việc thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi giao việc: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    public List<Task> myTasks(@RequestParam("employeeId") Long employeeId) {
        // Hàm này giờ đã khớp với Service
        return taskService.getTaskByEmployee(employeeId);
    }

    @PostMapping(value = "/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> report(poly.edu.vantix_hrm.entity.TaskAssignment dummy, // Để Spring ko bắt bẻ format
                                    org.springframework.web.multipart.MultipartHttpServletRequest request) {
        try {
            // 1. Tự tay lấy các field text
            String taskId = request.getParameter("taskId");
            String employeeId = request.getParameter("employeeId");
            String status = request.getParameter("status");

            System.out.println("!!!!!! ĐÃ NHẬN REQUEST TỪ MULTIPART !!!!!!");
            System.out.println("Task ID nhận được: " + taskId);

            // 2. Tự tay lấy file bằng đúng tên key "file" bên Vue gửi lên
            MultipartFile file = request.getFile("file");

            if (file == null || file.isEmpty()) {
                System.out.println("======> VẪN KHÔNG THẤY FILE TRONG REQUEST! <======");
                return ResponseEntity.badRequest().body("Vui lòng đính kèm file minh chứng!");
            }

            System.out.println("======> ĐÃ TÓM ĐƯỢC FILE: " + file.getOriginalFilename());

            // 3. Logic xử lý cũ của anh
            Task task = taskRepo.findById(Long.parseLong(taskId)).orElseThrow();

            // Upload Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
            task.setFileUrl(uploadResult.get("secure_url").toString());

            if (status != null) {
                task.setStatus(poly.edu.vantix_hrm.entity.TaskStatus.valueOf(status.toUpperCase()));
            }

            taskRepo.save(task);
            return ResponseEntity.ok("Báo cáo thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveTask(@PathVariable Long id) {
        try {
            Task task = taskRepo.findById(id).orElseThrow();
            task.setStatus(TaskStatus.COMPLETED);
            taskRepo.save(task);
            return ResponseEntity.ok("Phê duyệt thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelTask(@PathVariable Long id) {
        try {
            Task task = taskRepo.findById(id).orElseThrow();
            task.setStatus(TaskStatus.CANCELLED);
            taskRepo.save(task);
            return ResponseEntity.ok("Đã hủy công việc!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/reopen")
    public ResponseEntity<?> reopenTask(@PathVariable Long id) {
        try {
            Task task = taskRepo.findById(id).orElseThrow();
            task.setStatus(TaskStatus.IN_PROGRESS);
            taskRepo.save(task);
            return ResponseEntity.ok("Đã trả lại task cho nhân viên!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

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
}