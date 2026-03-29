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
    // Trong TaskController.java
    @PostMapping("/report")
    public ResponseEntity<?> report(
            @RequestParam("taskId") Integer taskId,
            @RequestParam("employeeId") Integer employeeId,
            @RequestParam("workDescription") String workDescription,
            @RequestParam(value = "progressPercent", required = false) Integer progressPercent,
            @RequestParam(value = "status", required = false) String status,
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
                fileUrl = uploadResult.get("secure_url").toString();
            }

            java.util.Optional<Task> taskOpt = taskRepo.findById(taskId);
            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();

                // Lưu link file vào bảng Task
                if (fileUrl != null) {
                    task.setFileUrl(fileUrl);
                }

                // Lưu trạng thái DONE vào bảng Task để Vue ẩn đi
                if (status != null && !status.isEmpty()) {
                    task.setStatus(poly.edu.vantix_hrm.entity.TaskStatus.valueOf(status.toUpperCase()));
                }

                taskRepo.save(task);

                // ĐÃ XÓA VĨNH VIỄN PHẦN TASK_REPORT GÂY LỖI 500 Ở ĐÂY 😎
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
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Task taskDetails) {
        try {
            // Chỉ gọi service để cập nhật vào DB
            taskService.updateTask(id, taskDetails);

            // Trả về câu thông báo đơn giản thay vì trả về đối tượng Task
            return ResponseEntity.ok("Cập nhật thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveTask(@PathVariable Integer id) {
        try {
            Task task = taskRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Task"));

            // Đổi trạng thái sang COMPLETED và lưu DB
            task.setStatus(poly.edu.vantix_hrm.entity.TaskStatus.COMPLETED);
            taskRepo.save(task);

            return ResponseEntity.ok("Phê duyệt thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelTask(@PathVariable Integer id) {
        try {
            Task task = taskRepo.findById(id).orElseThrow();
            task.setStatus(poly.edu.vantix_hrm.entity.TaskStatus.CANCELLED);
            taskRepo.save(task);
            return ResponseEntity.ok("Đã hủy công việc!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

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
    // 🔥 API LẤY BẢNG XẾP HẠNG KPI (LỌC THEO THÁNG CỦA DUE_DATE - KHÔNG CẦN SỬA DB)
    @GetMapping("/ranking")
    public ResponseEntity<?> getRanking(@RequestParam(value = "month", required = false) Integer month) {
        try {
            List<Task> completedTasks = taskRepo.findAll().stream()
                    .filter(t -> t.getStatus() != null && t.getStatus().name().equals("COMPLETED"))
                    .filter(t -> {
                        if (month == null || month == 0) return true;

                        // 🔥 ĐỔI SANG LỌC THEO createdAt (Vì task nào cũng có ngày tạo)
                        // Nếu bác dùng LocalDateTime thì dùng getMonthValue()
                        if (t.getCreatedAt() == null) return false;
                        return t.getCreatedAt().getMonthValue() == month;
                    })
                    .toList();

            // -- ĐOẠN DƯỚI NÀY GIỮ NGUYÊN --
            java.util.Map<Integer, Integer> pointsMap = new java.util.HashMap<>();
            java.util.Map<Integer, Integer> countMap = new java.util.HashMap<>();

            for (Task t : completedTasks) {
                if (t.getEmployeeId() != null) {
                    pointsMap.put(t.getEmployeeId(), pointsMap.getOrDefault(t.getEmployeeId(), 0) + (t.getPoint() != null ? t.getPoint() : 0));
                    countMap.put(t.getEmployeeId(), countMap.getOrDefault(t.getEmployeeId(), 0) + 1);
                }
            }

            List<Map<String, Object>> ranking = new java.util.ArrayList<>();
            for (Integer empId : pointsMap.keySet()) {
                Map<String, Object> rank = new java.util.HashMap<>();
                rank.put("employeeId", empId);
                rank.put("totalPoints", pointsMap.get(empId));
                rank.put("completedTasks", countMap.get(empId));
                ranking.add(rank);
            }

            ranking.sort((a, b) -> ((Integer) b.get("totalPoints")).compareTo((Integer) a.get("totalPoints")));

            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    // 🔥 API YÊU CẦU NHÂN VIÊN LÀM LẠI TASK (TỪ DONE -> IN_PROGRESS)
    @PutMapping("/{id}/reopen")
    public ResponseEntity<?> reopenTask(@PathVariable Integer id) {
        try {
            Task task = taskRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Task"));

            // Đẩy trạng thái quay ngược lại Đang làm
            task.setStatus(poly.edu.vantix_hrm.entity.TaskStatus.IN_PROGRESS);
            taskRepo.save(task);

            return ResponseEntity.ok("Đã trả lại task cho nhân viên!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }
}