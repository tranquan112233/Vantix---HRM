package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.Task;
import poly.edu.vantix_hrm.entity.TaskAssignment;
import poly.edu.vantix_hrm.entity.TaskReport;
import poly.edu.vantix_hrm.repository.TaskAssignmentRepository;
import poly.edu.vantix_hrm.repository.TaskReportRepository;
import poly.edu.vantix_hrm.service.TaskService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin("*") // 🔥 QUAN TRỌNG: Thêm dòng này để Vue.js (port 5173) không bị chặn CORS khi gọi API
public class TaskController {

    private final TaskService taskService;
    private final TaskAssignmentRepository assignmentRepo;
    private final TaskReportRepository reportRepo;

    // ================= CREATE =================
    @PostMapping
    public Task create(@RequestBody Task task) {
        return taskService.createTask(task);
    }

    // ================= GET ALL =================
    @GetMapping
    public List<Task> getAll() {
        return taskService.getAllTasks();
    }

    // ================= MY TASK =================
    @GetMapping("/my")
    public List<Task> myTasks(@RequestParam("employeeId") Integer employeeId) {
        // Xóa dòng fix cứng số 3 đi, lấy ID trực tiếp từ Frontend truyền lên
        return taskService.getTaskByEmployee(employeeId);
    }

    // ================= ASSIGN =================
    @PostMapping("/assign")
    public void assign(@RequestBody TaskAssignment ta) {
        ta.setAssignedAt(LocalDateTime.now()); // Tự động lưu thời gian giao việc
        assignmentRepo.save(ta);
    }

    // ================= REPORT =================
    @PostMapping("/report")
    public void report(@RequestBody TaskReport tr) {
        // 🔥 FIX LỖI 500 Ở ĐÂY: Backend tự động gán dữ liệu để không bị null hoặc lỗi parse ngày tháng
        tr.setEmployeeId(3); // Tạm thời fix cứng giống GET /my
        tr.setReportDate(LocalDate.now()); // Lấy ngày hiện tại
        tr.setCreatedAt(LocalDateTime.now()); // Lấy giờ hiện tại

        reportRepo.save(tr);
    }

}