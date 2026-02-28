package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens")
@Data
@AllArgsConstructor
@NoArgsConstructor
// Công dụng: Dùng để xác thực và đổi mật khẩu
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Integer id; // ID token

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // ID người dùng

    @Column(name = "token", nullable = false)
    private String token; // Token

    // Loại (Mã OTP,Đổi mật khẩu, Xác thực email, Xác thực 2 bước)
    public enum TokenType {
        OTP,
        RESET_PASSWORD,
        VERIFY_EMAIL,
        TWO_FACTOR_AUTH
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenType tokenType; // Loại token

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // Thời gian hết hạn

    @Column(name = "used")
    private boolean used; // Đã sử dụng chưa

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // Ngày tạo

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
