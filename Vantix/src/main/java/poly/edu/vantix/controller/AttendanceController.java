package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.vantix.dto.request.AttendanceCheckRequest;
import poly.edu.vantix.dto.request.AttendanceGenerateRequest;
import poly.edu.vantix.dto.response.AttendanceGenerateResponse;
import poly.edu.vantix.dto.response.AttendanceResponse;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.exception.UnauthorizedException;
import poly.edu.vantix.repository.EmployeeRepository;
import poly.edu.vantix.security.JwtUserPrincipal;
import poly.edu.vantix.service.AttendanceService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService service;
    private final EmployeeRepository employeeRepository;

    public AttendanceController(AttendanceService service, EmployeeRepository employeeRepository) {
        this.service = service;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ATTENDANCE_VIEW_ALL')")
    public ResponseEntity<List<AttendanceResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long employeeId
    ) {
        return ResponseEntity.ok(service.search(from, to, employeeId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<AttendanceResponse>> my(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(service.search(from, to, employeeIdFor(userId)));
    }

    @GetMapping("/me/today")
    public ResponseEntity<AttendanceResponse> today(Authentication authentication) {
        AttendanceResponse today = service.getTodayForUser(currentUserId(authentication));
        return today == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(today);
    }

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceResponse> checkIn(
            Authentication authentication,
            @Valid @RequestBody AttendanceCheckRequest request
    ) {
        return ResponseEntity.ok(service.checkIn(currentUserId(authentication), request));
    }

    @PostMapping("/check-out")
    public ResponseEntity<AttendanceResponse> checkOut(
            Authentication authentication,
            @Valid @RequestBody AttendanceCheckRequest request
    ) {
        return ResponseEntity.ok(service.checkOut(currentUserId(authentication), request));
    }

    @PostMapping("/generate-from-schedules")
    @PreAuthorize("hasAuthority('ATTENDANCE_VIEW_ALL')")
    public ResponseEntity<AttendanceGenerateResponse> generateFromSchedules(
            Authentication authentication,
            @Valid @RequestBody AttendanceGenerateRequest request
    ) {
        return ResponseEntity.ok(service.generateFromSchedules(currentEmployee(authentication), request));
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal principal)) {
            throw new UnauthorizedException("You are not signed in");
        }
        return principal.getId();
    }

    private Employee currentEmployee(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return employeeRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new BusinessException("No employee profile linked to your account"));
    }

    // Reuse: service.search with employeeId is enough; small helper to read from JWT.
    private Long employeeIdFor(Long userId) {
        // The service relies on employee resolution internally for /today, but list endpoints
        // accept employeeId. We resolve via a lightweight call so /me filter works for any user.
        // Avoid extra repo dependency by deferring to service through search using employeeId=null
        // when we cannot resolve. But to keep it strict, we resolve via the AttendanceService:
        return service.resolveEmployeeIdByUserId(userId);
    }
}
