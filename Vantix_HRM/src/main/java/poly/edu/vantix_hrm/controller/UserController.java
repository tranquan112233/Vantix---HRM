package poly.edu.vantix_hrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.page.PageRequestDTO;
import poly.edu.vantix_hrm.dto.page.PageResponseDTO;
import poly.edu.vantix_hrm.dto.user.*;
import poly.edu.vantix_hrm.entity.User.UserStatus;
import poly.edu.vantix_hrm.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users?keyword=&status=&page=0&size=10&sortBy=createdAt&sortDir=desc
    @PreAuthorize("hasAuthority('USER_VIEW')")
    @GetMapping
    public ResponseEntity<PageResponseDTO<UserResponseDTO>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @ModelAttribute PageRequestDTO pageRequest) {
        return ResponseEntity.ok(userService.getAll(keyword, status, pageRequest));
    }

    // GET /api/users/{id}
    @PreAuthorize("hasAuthority('USER_VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    // POST /api/users
    @PreAuthorize("hasAuthority('USER_CREATE')")
    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    // PUT /api/users/{id}
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    // DELETE /api/users/{id}
    @PreAuthorize("hasAuthority('USER_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/users/{id}/status?status=LOCKED
    @PreAuthorize("hasAuthority('USER_STATUS_UPDATE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponseDTO> changeStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status) {
        return ResponseEntity.ok(userService.changeStatus(id, status));
    }
}