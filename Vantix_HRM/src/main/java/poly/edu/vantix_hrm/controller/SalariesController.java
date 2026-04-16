package poly.edu.vantix_hrm.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.service.SalariesService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;


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

    // Import thêm các thư viện này ở đầu file:
    // import org.springframework.core.io.InputStreamResource;
    // import org.springframework.core.io.Resource;
    // import org.springframework.http.HttpHeaders;
    // import org.springframework.http.MediaType;
    // import java.io.ByteArrayInputStream;

    // API Xuất file Excel
    @GetMapping("/export")
    public ResponseEntity<Resource> exportExcel(@RequestParam("month") int month, @RequestParam("year") int year) {
        try {
            ByteArrayInputStream in = salariesService.exportSalariesToExcel(month, year);
            String fileName = "Bang_Luong_T" + month + "_" + year + ".xlsx";
            InputStreamResource file = new InputStreamResource(in);

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName).contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(file);
        } catch (Exception e) {
            e.printStackTrace(); // Quan trọng để soi lỗi nếu có
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // API Xuất 1 phiếu lương chi tiết
    @GetMapping("/export/{id}")
    public ResponseEntity<Resource> exportSinglePayslip(@PathVariable("id") Integer id) {
        try {
            ByteArrayInputStream in = salariesService.exportSinglePayslipToExcel(id);
            String fileName = "Phieu_Luong_EMP_" + id + ".xlsx";
            InputStreamResource file = new InputStreamResource(in);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
}