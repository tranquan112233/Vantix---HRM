package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Công dụng: Tài khoản đăng nhập hệ thống
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId; // ID người dùng

    @Column(name = "username", nullable = false, unique = true)
    private String username; // Tên đăng nhập

    @Column(name = "email", unique = true, nullable = false)
    private String email; // Email đăng nhập / nhận thông báo

    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // Mật khẩu đã mã hóa

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // Vai trò người dùng

    public enum UserStatus {ACTIVE, LOCKED} // Trạng thái (Hoạt động, Bị Khóa)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE; // Trạng thái người dùng

    @Column(name = "last_login")
    private LocalDateTime lastLogin; // Lần đăng nhập gần nhất

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Ngày tạo

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}