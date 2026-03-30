package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
 * AuthToken — token xác thực (OTP, reset password, 2FA)
 *
 * Quan hệ:
 *   AuthToken → User : nhiều token thuộc về 1 user
 */
@Entity
@Table(name = "auth_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User sở hữu token
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Mã token (OTP hoặc chuỗi reset password)
    @Column(nullable = false)
    private String token;

    // Loại token
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;

    // Thời gian hết hạn
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // Đã sử dụng chưa — mặc định false
    @Builder.Default
    private boolean used = false;

    // Kiểm tra token còn hạn và chưa dùng
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiryDate);
    }

    // ─── Enum loại token ──────────────────────────────────────────────────────

    public enum TokenType {
        OTP,            // Mã xác thực OTP
        RESET_PASSWORD, // Đặt lại mật khẩu
        TWO_FACTOR      // Xác thực 2 bước
    }
}