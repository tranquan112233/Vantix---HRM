package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.page.PageRequestDTO;
import poly.edu.vantix_hrm.dto.page.PageResponseDTO;
import poly.edu.vantix_hrm.dto.permission.*;
import poly.edu.vantix_hrm.service.PermissionService;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    // GET /api/permissions?keyword=&page=0&size=10&sortBy=createdAt&sortDir=desc
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    @GetMapping
    public ResponseEntity<PageResponseDTO<PermissionResponseDTO>> getAll(
            @RequestParam(required = false) String keyword,
            @ModelAttribute PageRequestDTO pageRequest) {
        return ResponseEntity.ok(permissionService.getAll(keyword, pageRequest));
    }

    // GET /api/permissions/{id}
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getById(id));
    }

    // POST /api/permissions
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    @PostMapping
    public ResponseEntity<PermissionResponseDTO> create(@Valid @RequestBody PermissionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.create(request));
    }

    // PUT /api/permissions/{id}
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<PermissionResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequestDTO request) {
        return ResponseEntity.ok(permissionService.update(id, request));
    }

    // DELETE /api/permissions/{id}
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}