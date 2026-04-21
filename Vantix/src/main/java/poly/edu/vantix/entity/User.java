package poly.edu.vantix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.UserStatus;

import java.time.LocalDateTime;

/*
 * User
 * - Tài khoản đăng nhập hệ thống
 * - Mỗi user chỉ có đúng 1 role
 * - Username không cho phép đổi sau khi tạo
 */
@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    // Tên đăng nhập duy nhất, không cho update
    @Column(name = "username", nullable = false, unique = true, updatable = false, length = 50)
    private String username;

    // Email duy nhất
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    // Mật khẩu đã mã hóa BCrypt
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    // Mỗi user thuộc 1 role
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // Trạng thái tài khoản
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    // Lần đăng nhập gần nhất
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // Lần hoạt động gần nhất
    @Column(name = "last_active")
    private LocalDateTime lastActive;
}