package poly.edu.vantix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "password_reset_otps",
        indexes = {
                @Index(name = "idx_password_reset_request", columnList = "request_id"),
                @Index(name = "idx_password_reset_token", columnList = "reset_token")
        }
)
public class PasswordResetOtp extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "request_id", nullable = false, unique = true, length = 64)
    private String requestId;

    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otpHash;

    @Column(name = "reset_token", unique = true, length = 100)
    private String resetToken;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;
}
