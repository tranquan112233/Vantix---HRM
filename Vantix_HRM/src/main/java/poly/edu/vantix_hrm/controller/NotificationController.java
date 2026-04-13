package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.Notification;
import poly.edu.vantix_hrm.entity.Role;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.repository.RoleRepository;
import poly.edu.vantix_hrm.repository.UserRepository;
import poly.edu.vantix_hrm.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor // Tự động inject NotificationService
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;



    // 1. Lấy danh sách thông báo của tôi (Nhân viên/Admin tự gọi cho chính mình)
    @GetMapping("/my")
    public ResponseEntity<List<Notification>> getMyNotifications(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getMyNotifications(userId));
    }

    // 2. Đánh dấu một thông báo là đã đọc
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 3. ADMIN GỬI LỆNH TRIỆU TẬP (SUMMON)
     * Dùng để "kêu" nhân viên lên phòng họp, phòng admin, v.v.
     */
    @PostMapping("/summon")
    public ResponseEntity<String> sendSummon(
            @RequestParam Long recipientId,
            @RequestParam String location,
            @RequestParam String reason) {

        String title = "⚡ LỆNH TRIỆU TẬP";
        String message = "Vui lòng đến " + location + " ngay bây giờ. Nội dung: " + reason;

        notificationService.sendNotification(
                recipientId,
                title,
                message,
                "SUMMON",
                "/dashboard" // Khi click vào sẽ về trang chủ
        );

        return ResponseEntity.ok("Đã gửi lệnh triệu tập thành công!");
    }

    /**
     * 4. THÔNG BÁO TÙY CHỈNH (Dùng cho các trường hợp khác)
     */
    @PostMapping("/send-custom")
    public ResponseEntity<String> sendCustomNotification(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam String type) {

        notificationService.sendNotification(userId, title, message, type, null);
        return ResponseEntity.ok("Đã gửi thông báo!");
    }

    @DeleteMapping("/delete-all-except-starred")
    public ResponseEntity<Void> deleteAllExceptStarred(@RequestParam Long userId) {
        notificationService.deleteAllExceptStarred(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/summon-bulk")
    public ResponseEntity<String> sendBulkSummon(
            @RequestParam String roleName,
            @RequestParam String location,
            @RequestParam String reason) {

        notificationService.sendBulkSummon(roleName, location, reason);
        return ResponseEntity.ok("Đã phát lệnh hàng loạt thành công!");
    }

    @GetMapping("/recipient-list")
    public ResponseEntity<List<Object[]>> getRecipientList(@RequestParam(defaultValue = "ALL") String roleName) {
        // Truyền thêm User.UserStatus.ACTIVE vào hàm
        return ResponseEntity.ok(userRepository.findActiveRecipientsByRole(roleName, User.UserStatus.ACTIVE));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findByDeletedFalse());
    }
}