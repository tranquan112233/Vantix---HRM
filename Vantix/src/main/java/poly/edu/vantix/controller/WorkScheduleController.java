package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.vantix.dto.request.WorkScheduleBulkRequest;
import poly.edu.vantix.dto.request.WorkScheduleRequest;
import poly.edu.vantix.dto.response.WorkScheduleBulkResponse;
import poly.edu.vantix.dto.response.WorkScheduleResponse;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.exception.UnauthorizedException;
import poly.edu.vantix.repository.EmployeeRepository;
import poly.edu.vantix.security.JwtUserPrincipal;
import poly.edu.vantix.service.WorkScheduleService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/work-schedules")
public class WorkScheduleController {

    private final WorkScheduleService service;
    private final EmployeeRepository employeeRepository;

    public WorkScheduleController(WorkScheduleService service, EmployeeRepository employeeRepository) {
        this.service = service;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCHEDULE_VIEW_ALL')")
    public ResponseEntity<List<WorkScheduleResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId
    ) {
        return ResponseEntity.ok(service.search(from, to, employeeId, departmentId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<WorkScheduleResponse>> my(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Employee employee = currentEmployee(authentication);
        return ResponseEntity.ok(service.search(from, to, employee.getId(), null));
    }

    @GetMapping("/me/today")
    public ResponseEntity<WorkScheduleResponse> today(Authentication authentication) {
        Employee employee = currentEmployee(authentication);
        return service.findByEmployeeAndDate(employee.getId(), LocalDate.now())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCHEDULE_CREATE')")
    public ResponseEntity<WorkScheduleResponse> create(@Valid @RequestBody WorkScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('SCHEDULE_CREATE')")
    public ResponseEntity<WorkScheduleBulkResponse> bulkCreate(@Valid @RequestBody WorkScheduleBulkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.bulkCreate(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCHEDULE_UPDATE')")
    public ResponseEntity<WorkScheduleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody WorkScheduleRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCHEDULE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Employee currentEmployee(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal principal)) {
            throw new UnauthorizedException("You are not signed in");
        }
        return employeeRepository.findActiveByUserId(principal.getId())
                .orElseThrow(() -> new BusinessException("No employee profile linked to your account"));
    }
}
