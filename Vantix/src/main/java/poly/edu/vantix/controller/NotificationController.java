package poly.edu.vantix.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix.dto.request.NotificationRequest;
import poly.edu.vantix.dto.response.NotificationResponse;
import poly.edu.vantix.entity.enums.NotificationStatus;
import poly.edu.vantix.entity.enums.NotificationType;
import poly.edu.vantix.exception.UnauthorizedException;
import poly.edu.vantix.security.JwtUserPrincipal;
import poly.edu.vantix.service.NotificationService;
import poly.edu.vantix.util.PageableUtils;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Lay danh sach thong bao cua user dang dang nhap
    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    public ResponseEntity<?> getMyNotifications(
            Authentication auth,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        Long userId = getPrincipalId(auth);

        if (PageableUtils.isPaged(page, size)) {
            return ResponseEntity.ok(notificationService.getByUserId(
                    userId,
                    status,
                    type,
                    PageableUtils.from(page, size)
            ));
        }

        return ResponseEntity.ok(notificationService.getByUserId(userId, status, type));
    }

    // Dem so thong bao chua doc
    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication auth) {
        Long userId = getPrincipalId(auth);
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(userId)));
    }

    // Gui thong bao moi (HR/Admin). Ho tro gui cho 1 user (userId),
    // nhieu user (userIds) hoac toan bo (sendToAll = true).
    @PostMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_SEND')")
    public ResponseEntity<List<NotificationResponse>> create(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.createBulk(request));
    }

    // Danh dau da doc 1 thong bao
    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    public ResponseEntity<NotificationResponse> markAsRead(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(getPrincipalId(auth), id));
    }

    // Danh dau tat ca da doc
    @PatchMapping("/read-all")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    public ResponseEntity<Void> markAllAsRead(Authentication auth) {
        Long userId = getPrincipalId(auth);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    // Xoa thong bao
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        notificationService.delete(getPrincipalId(auth), id);
        return ResponseEntity.noContent().build();
    }

    private Long getPrincipalId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof JwtUserPrincipal principal)) {
            throw new UnauthorizedException("You are not signed in");
        }
        return principal.getId();
    }
}
