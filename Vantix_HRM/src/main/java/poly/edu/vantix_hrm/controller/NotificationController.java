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
    // Trong NotificationController.java

    @PostMapping("/summon")
    public ResponseEntity<String> sendNotice(
            @RequestParam Long recipientId,
            @RequestParam String location,
            @RequestParam String reason,
            @RequestParam String priority) { // GẤP, BÌNH THƯỜNG, HẸN GẶP

        String title;
        String type;

        switch (priority) {
            case "URGENT":
                title = "⚡ LỆNH TRIỆU TẬP KHẨN";
                type = "SUMMON"; // Hiện màu đỏ
                break;
            case "MEETING":
                title = "📅 LỊCH HẸN GẶP";
                type = "TASK"; // Hiện màu xanh dương
                break;
            default:
                title = "📩 THÔNG BÁO MỜI";
                type = "INFO"; // Hiện màu xanh lá/cyan
        }

        notificationService.sendNotification(recipientId, title, reason + " tại " + location, type, "/my-notifications");
        return ResponseEntity.ok("Đã gửi thành công!");
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

    // 1. Xóa một thông báo cụ thể (Sửa lỗi DELETE 500)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        // Gọi hàm softDelete để đổi trạng thái is_deleted = true trong DB
        notificationService.softDelete(id);
        return ResponseEntity.ok().build();
    }

    // 2. Đảo trạng thái Star (Yêu thích)
    @PutMapping("/{id}/star")
    public ResponseEntity<Void> toggleStar(@PathVariable Long id) {
        notificationService.toggleStar(id); // Gọi hàm toggleStar trong Service
        return ResponseEntity.ok().build();
    }

    // 3. Đánh dấu đọc tất cả
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestParam Long userId) {
        // Bạn cần viết thêm hàm này trong Service hoặc xử lý nhanh tại đây
        List<Notification> unread = notificationService.getMyNotifications(userId);
        unread.stream().filter(n -> !n.isRead()).forEach(n -> notificationService.markAsRead(n.getId()));
        return ResponseEntity.ok().build();
    }

    // 4. Dọn dẹp (Xóa tất cả trừ mục có sao)
    @DeleteMapping("/clear-all")
    public ResponseEntity<Void> clearAll(@RequestParam Long userId) {
        notificationService.deleteAllExceptStarred(userId); // Gọi hàm dọn dẹp
        return ResponseEntity.ok().build();
    }
}