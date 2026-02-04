package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Users") // Công dụng: Tài khoản đăng nhập hệ thống
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId; // ID tài khoản

    @Column(name = "username", nullable = false, unique = true)
    private String username; // Tên đăng nhập

    @Column(name = "email", unique = true)
    private String email; // Email đăng nhập / nhận thông báo

    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // Mật khẩu đã mã hóa

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Roles role; // Vai trò người dùng

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status = UserStatus.ACTIVE; // Trạng thái tài khoản

    @Column(name = "last_login")
    private LocalDateTime lastLogin; // Lần đăng nhập gần nhất

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Ngày tạo

    public enum UserStatus {ACTIVE, LOCKED}
}