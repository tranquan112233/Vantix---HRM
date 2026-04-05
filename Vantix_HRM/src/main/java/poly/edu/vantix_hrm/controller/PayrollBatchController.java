package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.PayrollBatch;
import poly.edu.vantix_hrm.service.PayrollBatchService;

@RestController
@RequestMapping("/api/payroll-batches")
@CrossOrigin("*")
@RequiredArgsConstructor
public class PayrollBatchController {

    private final PayrollBatchService payrollBatchService;

    // Lấy danh sách toàn bộ các đợt lương
    @GetMapping
    public ResponseEntity<?> getAllBatches() {
        try {
            return ResponseEntity.ok(payrollBatchService.getAllPayrollBatches());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // API Chốt bảng lương tháng
    @PostMapping("/finalize/{month}/{year}")
    public ResponseEntity<?> finalizePayroll(@PathVariable("month") int month, @PathVariable("year") int year) {
        try {
            return ResponseEntity.ok(payrollBatchService.finalizePayrollBatch(month, year));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // API Phê duyệt / Từ chối đợt lương
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBatchStatus(@PathVariable("id") Integer id, @RequestParam("status") String status) {
        try {
            PayrollBatch.BatchStatus newStatus = PayrollBatch.BatchStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(payrollBatchService.updateBatchStatus(id, newStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Trạng thái truyền lên không hợp lệ!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}