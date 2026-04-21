package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix.dto.request.RoleRequest;
import poly.edu.vantix.dto.response.PermissionResponse;
import poly.edu.vantix.dto.response.RoleResponse;
import poly.edu.vantix.service.RoleService;
import poly.edu.vantix.util.PageableUtils;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // Danh sách role + tìm kiếm
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_VIEW','USER_VIEW','USER_CREATE','USER_UPDATE','EMPLOYEE_CREATE','EMPLOYEE_UPDATE')")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (PageableUtils.isPaged(page, size)) {
            return ResponseEntity.ok(roleService.searchPage(keyword, PageableUtils.from(page, size)));
        }

        return ResponseEntity.ok(roleService.search(keyword));
    }

    // Chi tiết role
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<RoleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getById(id));
    }

    // Tạo mới role
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(request));
    }

    // Cập nhật role
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<RoleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request
    ) {
        return ResponseEntity.ok(roleService.update(id, request));
    }

    // Xóa mềm role
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
