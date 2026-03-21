package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.Task;
import poly.edu.vantix_hrm.entity.TaskAssignment;
import poly.edu.vantix_hrm.entity.TaskReport;
import poly.edu.vantix_hrm.repository.TaskAssignmentRepository;
import poly.edu.vantix_hrm.repository.TaskReportRepository;
import poly.edu.vantix_hrm.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
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
    public List<Task> myTasks() {
        Integer employeeId = 3; // demo
        return taskService.getTaskByEmployee(employeeId);
    }

    // ================= ASSIGN =================
    @PostMapping("/assign")
    public void assign(@RequestBody TaskAssignment ta) {
        assignmentRepo.save(ta);
    }

    // ================= REPORT =================
    @PostMapping("/report")
    public void report(@RequestBody TaskReport tr) {
        reportRepo.save(tr);
    }

}
