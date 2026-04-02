package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
 * User — tài khoản người dùng trong hệ thống
 *
 * Ví dụ: Admin, Manager, Employee
 *
 * Quan hệ:
 *   User → Role : nhiều User thuộc 1 Role
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên đăng nhập — không được trùng
    @Column(nullable = false, unique = true)
    private String username;

    // Mật khẩu — mã hóa bằng BCrypt
    @Column(nullable = false)
    private String password;

    // Email — không được trùng
    @Column(nullable = false, unique = true)
    private String email;

    // Role của user — nhiều user có thể có cùng 1 role
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    // Thời điểm hoạt động gần nhất — dùng để xác định online/offline
    private LocalDateTime lastActive;

    // Trạng thái tài khoản
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    // ─── Enum trạng thái ─────────────────────────────────────────────────────

    public enum UserStatus {
        ACTIVE,  // Đang hoạt động
        LOCKED   // Bị khóa
    }
}
