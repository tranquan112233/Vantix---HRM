package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID của nhân viên nhận thông báo (Liên kết với bảng employees)
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    // Tiêu đề thông báo (VD: "Lệnh triệu tập", "Công việc mới")
    @Column(nullable = false)
    private String title;

    // Nội dung chi tiết (VD: "Vui lòng lên phòng họp gấp", "Bạn được giao task X")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    /**
     * Phân loại thông báo để xử lý logic ở Frontend
     * Các loại: LEAVE, TASK, OVERTIME, SUMMON (Triệu tập), SYSTEM
     */
    @Column(length = 50)
    private String type;

    // Đường dẫn để khi click vào thông báo sẽ nhảy tới trang đó (VD: /admin/leave-request)
    @Column(name = "target_url")
    private String targetUrl;

    // Trạng thái đã đọc hay chưa
    @Builder.Default
    @Column(name = "is_read")
    private boolean isRead = false;

    // Thời gian tạo thông báo
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Tự động gán thời gian khi tạo mới
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Notification.java
    @Column(name = "is_starred") // Chỉ định rõ tên cột trong MySQL
    private boolean isStarred = false;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;
}