package poly.edu.vantix_hrm.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.service.SalariesService;

@RestController
@RequestMapping("api/salaries")
@CrossOrigin("*")
@RequiredArgsConstructor
public class SalariesController {

    private final SalariesService salariesService;

    // Lấy toàn bộ tên phòng ban
    @GetMapping("/departments")
    public ResponseEntity<?> getDepartmentNames() {
        try {
            return ResponseEntity.ok(salariesService.findDepartmentNames());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // lấy danh sách lương theo tháng/năm
    @GetMapping
    public ResponseEntity<?> getSalariesByMonth(@RequestParam("month") int month, @RequestParam("year") int year) {
        try {
            return ResponseEntity.ok(salariesService.getSalariesByMonthAndYear(month, year));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Chốt toàn danh sách lương sang trạng thái Chờ Duyệt (PENDING)
    @PutMapping("/submit-all-pending")
    public ResponseEntity<?> submitAllPending(@RequestParam("month") int month, @RequestParam("year") int year) {
        try {
            int count = salariesService.submitAllSalariesToPending(month, year);
            return ResponseEntity.ok("Đã chuyển " + count + " bản lương sang trạng thái Chờ Duyệt (PENDING).");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // API Tự động tính toán bảng lương cho toàn bộ nhân viên trong tháng
    @PostMapping("/generate/{month}/{year}")
    public ResponseEntity<?> generateSalaries(@PathVariable("month") int month, @PathVariable("year") int year) {
        try {
            String resultMsg = salariesService.generateSalaries(month, year);
            return ResponseEntity.ok(resultMsg);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống khi tính lương: " + e.getMessage());
        }
    }
}