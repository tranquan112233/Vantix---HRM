package poly.edu.vantix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.NotificationStatus;
import poly.edu.vantix.entity.enums.NotificationType;

import java.time.LocalDateTime;

/*
 * Notification
 * - Thông báo gửi đến user trong hệ thống
 * - Có thể dùng cho chấm công, nghỉ phép, lương, thông báo chung
 */
@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    // User nhận thông báo
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Loại thông báo
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type = NotificationType.GENERAL;

    // Tiêu đề thông báo
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    // Nội dung thông báo
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    // Link điều hướng khi user bấm vào thông báo
    @Column(name = "target_url", length = 255)
    private String targetUrl;

    // Trạng thái đọc / chưa đọc
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.UNREAD;

    // Thời gian đã đọc thông báo
    @Column(name = "read_at")
    private LocalDateTime readAt;
}