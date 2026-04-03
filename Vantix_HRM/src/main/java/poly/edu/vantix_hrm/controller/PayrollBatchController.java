package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.service.PayrollBatchService;

@RestController
@RequestMapping("/api/payroll-batches")
@CrossOrigin("*")
@RequiredArgsConstructor
public class PayrollBatchController {

    private final PayrollBatchService payrollBatchService;

    // API Chốt bảng lương tháng
    @PostMapping("/finalize")
    public ResponseEntity<?> finalizePayroll(@RequestParam("month") int month, @RequestParam("year") int year) {
        try {
            return ResponseEntity.ok(payrollBatchService.finalizePayrollBatch(month, year));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}