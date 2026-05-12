package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix.dto.request.PayrollAdjustRequest;
import poly.edu.vantix.dto.request.PayrollPeriodRequest;
import poly.edu.vantix.dto.response.PayrollPeriodResponse;
import poly.edu.vantix.dto.response.PayrollResponse;
import poly.edu.vantix.exception.UnauthorizedException;
import poly.edu.vantix.security.JwtUserPrincipal;
import poly.edu.vantix.service.PayrollService;

import java.util.List;

@RestController
@RequestMapping("/api/payrolls")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    // ============== PERIOD ==============

    @GetMapping("/periods")
    @PreAuthorize("hasAnyAuthority('PAYROLL_VIEW','PAYROLL_MANAGE')")
    public ResponseEntity<List<PayrollPeriodResponse>> listPeriods(
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(payrollService.listPeriods(year));
    }

    @GetMapping("/periods/{id}")
    @PreAuthorize("hasAnyAuthority('PAYROLL_VIEW','PAYROLL_MANAGE')")
    public ResponseEntity<PayrollPeriodResponse> getPeriod(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.getPeriod(id));
    }

    @PostMapping("/periods")
    @PreAuthorize("hasAnyAuthority('PAYROLL_CREATE','PAYROLL_MANAGE')")
    public ResponseEntity<PayrollPeriodResponse> createPeriod(@Valid @RequestBody PayrollPeriodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollService.createPeriod(request));
     }

    @PutMapping("/periods/{id}")
    @PreAuthorize("hasAnyAuthority('PAYROLL_UPDATE','PAYROLL_MANAGE')")
    public ResponseEntity<PayrollPeriodResponse> updatePeriod(
            @PathVariable Long id,
            @Valid @RequestBody PayrollPeriodRequest request
    ) {
        return ResponseEntity.ok(payrollService.updatePeriod(id, request)); 
    }

    @DeleteMapping("/periods/{id}")
    @PreAuthorize("hasAnyAuthority('PAYROLL_DELETE','PAYROLL_MANAGE')")
    public ResponseEntity<Void> deletePeriod(@PathVariable Long id) {
        payrollService.deletePeriod(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/periods/{id}/generate")
    @PreAuthorize("hasAnyAuthority('PAYROLL_CREATE','PAYROLL_MANAGE')")
    public ResponseEntity<PayrollPeriodResponse> generate(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.generate(id));
    }

    @PostMapping("/periods/{id}/recalculate")
    @PreAuthorize("hasAnyAuthority('PAYROLL_UPDATE','PAYROLL_MANAGE')")
    public ResponseEntity<PayrollPeriodResponse> recalculate(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.recalculateAll(id));
    }

    @PostMapping("/periods/{id}/approve")
    @PreAuthorize("hasAnyAuthority('PAYROLL_APPROVE','PAYROLL_MANAGE')")
    public ResponseEntity<PayrollPeriodResponse> approve(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(payrollService.approve(id, currentUserId(authentication)));
    }

    @PostMapping("/periods/{id}/pay")
    @PreAuthorize("hasAnyAuthority('PAYROLL_PAY','PAYROLL_MANAGE')")
    public ResponseEntity<PayrollPeriodResponse> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.markPaid(id));
    }

    // ============== PAYROLL ROWS ==============

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PAYROLL_VIEW','PAYROLL_MANAGE')")
    public ResponseEntity<List<PayrollResponse>> listPayrolls(
            @RequestParam Long periodId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId
    ) {
        return ResponseEntity.ok(payrollService.listPayrolls(periodId, keyword, departmentId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<PayrollResponse>> myPayrolls(Authentication authentication) {
        return ResponseEntity.ok(payrollService.myPayrolls(currentUserId(authentication)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PAYROLL_VIEW','PAYROLL_MANAGE')")
    public ResponseEntity<PayrollResponse> getPayroll(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.getPayroll(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PAYROLL_UPDATE','PAYROLL_MANAGE')")
    public ResponseEntity<PayrollResponse> adjust(
            @PathVariable Long id,
            @Valid @RequestBody PayrollAdjustRequest request
    ) {
        return ResponseEntity.ok(payrollService.adjustAndRecalculate(id, request));
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal principal)) {
            throw new UnauthorizedException("You are not signed in");
        }
        return principal.getId();
    }
}
