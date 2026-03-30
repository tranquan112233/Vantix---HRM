package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.employee.EmployeeRequestDTO;
import poly.edu.vantix_hrm.dto.employee.EmployeeResponseDTO;
import poly.edu.vantix_hrm.dto.page.PageRequestDTO;
import poly.edu.vantix_hrm.dto.page.PageResponseDTO;
import poly.edu.vantix_hrm.entity.Employee.WorkStatus;
import poly.edu.vantix_hrm.service.EmployeeService;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // GET /api/employees?keyword=&workStatus=&departmentId=&page=0&size=10&sortBy=createdAt&sortDir=desc
    @GetMapping
    public ResponseEntity<PageResponseDTO<EmployeeResponseDTO>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) WorkStatus workStatus,
            @RequestParam(required = false) Long departmentId,
            @ModelAttribute PageRequestDTO pageRequest) {
        return ResponseEntity.ok(employeeService.getAll(keyword, workStatus, departmentId, pageRequest));
    }

    // GET /api/employees/{id}
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    // GET /api/employees/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<EmployeeResponseDTO> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(employeeService.getByUserId(userId));
    }

    // POST /api/employees
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> create(@Valid @RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    // PUT /api/employees/{id}
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    // DELETE /api/employees/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}