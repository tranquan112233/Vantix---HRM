package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix.dto.request.EmployeeRequest;
import poly.edu.vantix.dto.response.EmployeeResponse;
import poly.edu.vantix.entity.enums.EmploymentStatus;
import poly.edu.vantix.entity.enums.Gender;
import poly.edu.vantix.service.EmployeeService;
import poly.edu.vantix.util.PageableUtils;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // Danh sách nhân viên + tìm kiếm
    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_VIEW')")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (PageableUtils.isPaged(page, size)) {
            return ResponseEntity.ok(employeeService.searchPage(
                    keyword,
                    departmentId,
                    gender,
                    status,
                    PageableUtils.from(page, size)
            ));
        }

        return ResponseEntity.ok(employeeService.search(keyword, departmentId, gender, status));
    }

    // Chi tiết nhân viên
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_VIEW')")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    // Tạo mới nhân viên
    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    // Cập nhật nhân viên
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request
    ) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    // Xóa mềm nhân viên
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
