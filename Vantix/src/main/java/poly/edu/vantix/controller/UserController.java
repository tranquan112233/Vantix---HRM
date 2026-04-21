package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix.dto.request.UserRequest;
import poly.edu.vantix.dto.response.UserResponse;
import poly.edu.vantix.entity.enums.UserStatus;
import poly.edu.vantix.service.UserService;
import poly.edu.vantix.util.PageableUtils;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Danh sách user + tìm kiếm + loc theo role/status
    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER_VIEW','NOTIFICATION_SEND')")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (PageableUtils.isPaged(page, size)) {
            return ResponseEntity.ok(userService.searchPage(keyword, roleId, status, PageableUtils.from(page, size)));
        }

        return ResponseEntity.ok(userService.search(keyword, roleId, status));
    }

    // Chi tiết user
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    // Tạo mới user
    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    // Cập nhật user
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request
    ) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    // Xóa mềm user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
