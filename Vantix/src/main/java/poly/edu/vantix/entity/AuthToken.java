package poly.edu.vantix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.TokenStatus;
import poly.edu.vantix.entity.enums.TokenType;

import java.time.LocalDateTime;

/*
 * AuthToken
 * - Lưu OTP hoặc token reset password
 * - Một user có thể có nhiều token
 */
@Getter
@Setter
@Entity
@Table(name = "auth_tokens")
public class AuthToken extends BaseEntity {

    // User sở hữu token
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Loại token
    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 30)
    private TokenType tokenType;

    // Giá trị token hoặc mã OTP
    @Column(name = "token", nullable = false, length = 255)
    private String token;

    // Trạng thái token
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TokenStatus status = TokenStatus.PENDING;

    // Thời gian hết hạn
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Thời gian đã dùng token
    @Column(name = "used_at")
    private LocalDateTime usedAt;
}