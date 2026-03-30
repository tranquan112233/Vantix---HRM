package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.department.DepartmentRequestDTO;
import poly.edu.vantix_hrm.dto.department.DepartmentResponseDTO;
import poly.edu.vantix_hrm.dto.page.PageRequestDTO;
import poly.edu.vantix_hrm.dto.page.PageResponseDTO;
import poly.edu.vantix_hrm.service.DepartmentService;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    // GET /api/departments?keyword=&page=0&size=10&sortBy=createdAt&sortDir=desc
    @GetMapping
    public ResponseEntity<PageResponseDTO<DepartmentResponseDTO>> getAll(
            @RequestParam(required = false) String keyword,
            @ModelAttribute PageRequestDTO pageRequest) {
        return ResponseEntity.ok(departmentService.getAll(keyword, pageRequest));
    }

    // GET /api/departments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    // POST /api/departments
    @PostMapping
    public ResponseEntity<DepartmentResponseDTO> create(@Valid @RequestBody DepartmentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(request));
    }

    // PUT /api/departments/{id}
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequestDTO request) {
        return ResponseEntity.ok(departmentService.update(id, request));
    }

    // DELETE /api/departments/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}