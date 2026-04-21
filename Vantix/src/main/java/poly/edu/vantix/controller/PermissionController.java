package poly.edu.vantix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.vantix.dto.response.PermissionResponse;
import poly.edu.vantix.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final RoleService roleService;

    public PermissionController(RoleService roleService) {
        this.roleService = roleService;
    }

    // Danh sách tất cả permission
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<List<PermissionResponse>> list() {
        return ResponseEntity.ok(roleService.getAllPermissions());
    }
}
