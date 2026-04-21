package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix.dto.request.DepartmentRequest;
import poly.edu.vantix.dto.response.DepartmentResponse;
import poly.edu.vantix.service.DepartmentService;
import poly.edu.vantix.util.PageableUtils;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // Danh sách phòng ban + tìm kiếm
    @GetMapping
    @PreAuthorize("hasAnyAuthority('DEPARTMENT_VIEW','SCHEDULE_VIEW_ALL','SCHEDULE_CREATE')")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (PageableUtils.isPaged(page, size)) {
            return ResponseEntity.ok(departmentService.searchPage(keyword, PageableUtils.from(page, size)));
        }

        return ResponseEntity.ok(departmentService.search(keyword));
    }

    // Chi tiết phòng ban
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DEPARTMENT_VIEW','SCHEDULE_VIEW_ALL','SCHEDULE_CREATE')")
    public ResponseEntity<DepartmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    // Tạo mới phòng ban
    @PostMapping
    @PreAuthorize("hasAuthority('DEPARTMENT_CREATE')")
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(request));
    }

    // Cập nhật phòng ban
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_UPDATE')")
    public ResponseEntity<DepartmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request
    ) {
        return ResponseEntity.ok(departmentService.update(id, request));
    }

    // Xóa mềm phòng ban
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
