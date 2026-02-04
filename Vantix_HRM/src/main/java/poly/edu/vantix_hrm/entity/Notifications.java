package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications") // Công dụng: Thông báo hệ thống
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId; // ID thông báo

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employees employee; // Người nhận (nếu gửi riêng)

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Roles role; // Gửi theo quyền (ví dụ gửi tất cả HR)

    @ManyToOne
    @JoinColumn(name = "position_id")
    private Positions position; // Gửi theo chức vụ

    @Column(name = "title", length = 100)
    private String title; // Tiêu đề

    @Column(name = "content")
    private String content; // Nội dung

    @Column(name = "attachment_url", length = 255)
    private String attachmentUrl; // File đính kèm

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private Employees sender; // Người gửi

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(); // Thời gian tạo
}