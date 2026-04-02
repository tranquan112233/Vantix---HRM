package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.employee.EmployeeRequestDTO;
import poly.edu.vantix_hrm.dto.employee.EmployeeResponseDTO;
import poly.edu.vantix_hrm.dto.page.PageRequestDTO;
import poly.edu.vantix_hrm.dto.page.PageResponseDTO;
import poly.edu.vantix_hrm.dto.profile.UserProfileDTO;
import poly.edu.vantix_hrm.entity.Employee.WorkStatus;
import poly.edu.vantix_hrm.service.EmployeeService;


@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // GET /api/employees?keyword=&workStatus=&departmentId=&page=0&size=10&sortBy=createdAt&sortDir=desc
    @PreAuthorize("hasAuthority('EMPLOYEE_VIEW')")
    @GetMapping
    public ResponseEntity<PageResponseDTO<EmployeeResponseDTO>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) WorkStatus workStatus,
            @RequestParam(required = false) Long departmentId,
            @ModelAttribute PageRequestDTO pageRequest) {
        return ResponseEntity.ok(employeeService.getAll(keyword, workStatus, departmentId, pageRequest));
    }

    // GET /api/employees/{id}
    @PreAuthorize("hasAuthority('EMPLOYEE_VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    // GET /api/employees/user/{userId}
    @PreAuthorize("hasAuthority('EMPLOYEE_VIEW')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<EmployeeResponseDTO> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(employeeService.getByUserId(userId));
    }

    // POST /api/employees
    @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> create(@Valid @RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    // PUT /api/employees/{id}
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    // DELETE /api/employees/{id}
    @PreAuthorize("hasAuthority('EMPLOYEE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
    // API Lấy hồ sơ cá nhân theo Token
    @GetMapping("/my-profile")
    public ResponseEntity<UserProfileDTO> getMyProfile() {
        return ResponseEntity.ok(employeeService.getMyProfile());
    }
}