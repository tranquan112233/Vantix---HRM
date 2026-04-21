package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.vantix.dto.request.PayrollSettingRequest;
import poly.edu.vantix.dto.response.PayrollSettingResponse;
import poly.edu.vantix.service.PayrollSettingService;

@RestController
@RequestMapping("/api/payroll-settings")
public class PayrollSettingController {

    private final PayrollSettingService payrollSettingService;

    public PayrollSettingController(PayrollSettingService payrollSettingService) {
        this.payrollSettingService = payrollSettingService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PAYROLL_MANAGE')")
    public ResponseEntity<PayrollSettingResponse> get() {
        return ResponseEntity.ok(payrollSettingService.get());
    }

    @PatchMapping
    @PreAuthorize("hasAuthority('PAYROLL_MANAGE')")
    public ResponseEntity<PayrollSettingResponse> update(@Valid @RequestBody PayrollSettingRequest request) {
        return ResponseEntity.ok(payrollSettingService.update(request));
    }

    @PostMapping("/reset")
    @PreAuthorize("hasAuthority('PAYROLL_MANAGE')")
    public ResponseEntity<PayrollSettingResponse> reset() {
        return ResponseEntity.ok(payrollSettingService.reset());
    }
}
