package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.department.DepartmentRequest;
import poly.edu.vantix_hrm.dto.department.DepartmentResponse;
import poly.edu.vantix_hrm.service.DepartmentService;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    // =====================================================
    // GET ALL DEPARTMENTS
    // =====================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentResponse>> getAll() {
        return ResponseEntity.ok(departmentService.findAll());
    }

    // =====================================================
    // GET DEPARTMENT BY ID
    // =====================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> getById(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(departmentService.findById(id));
    }

    // =====================================================
    // CREATE DEPARTMENT
    // =====================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> create(
            @Valid @RequestBody DepartmentRequest request
    ) {
        return ResponseEntity.ok(departmentService.create(request));
    }

    // =====================================================
    // UPDATE DEPARTMENT
    // =====================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody DepartmentRequest request
    ) {
        return ResponseEntity.ok(departmentService.update(id, request));
    }

    // =====================================================
    // DELETE DEPARTMENT
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id
    ) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}