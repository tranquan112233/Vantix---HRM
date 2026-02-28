package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.role.CreateRoleRequest;
import poly.edu.vantix_hrm.dto.role.RoleResponse;
import poly.edu.vantix_hrm.dto.role.UpdateRoleRequest;
import poly.edu.vantix_hrm.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // =====================================================
    // GET ALL ROLES
    // =====================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleResponse>> getAll() {
        return ResponseEntity.ok(roleService.findAll());
    }

    // =====================================================
    // GET ROLE BY ID
    // =====================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> getById(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    // =====================================================
    // CREATE ROLE
    // =====================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> create(
            @Valid @RequestBody CreateRoleRequest request
    ) {
        return ResponseEntity.ok(roleService.create(request));
    }

    // =====================================================
    // UPDATE ROLE
    // =====================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return ResponseEntity.ok(roleService.update(id, request));
    }

    // =====================================================
    // DELETE ROLE
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id
    ) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}