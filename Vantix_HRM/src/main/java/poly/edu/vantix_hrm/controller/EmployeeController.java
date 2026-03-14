package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.UserProfileDTO;
import poly.edu.vantix_hrm.dto.employee.EmployeeCreateRequest;
import poly.edu.vantix_hrm.dto.employee.EmployeeResponse;
import poly.edu.vantix_hrm.dto.employee.EmployeeUpdateRequest;
import poly.edu.vantix_hrm.service.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /* ================= FIND ALL ================= */

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> findAll() {

        return ResponseEntity.ok(employeeService.findAll());
    }

    /* ================= FIND BY ID ================= */

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> findById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(employeeService.findById(id));
    }

    /* ================= CREATE ================= */

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(
            @Valid @RequestBody EmployeeCreateRequest request) {

        return ResponseEntity.ok(employeeService.create(request));
    }

    /* ================= UPDATE ================= */

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody EmployeeUpdateRequest request) {

        return ResponseEntity.ok(employeeService.update(id, request));
    }

    /* ================= DELETE ================= */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id) {

        employeeService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-profile")
    public ResponseEntity<UserProfileDTO> getMyProfile() {
        return ResponseEntity.ok(employeeService.getMyProfile());
    }
}