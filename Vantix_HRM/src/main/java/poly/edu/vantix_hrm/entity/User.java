package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum Status {
        ACTIVE,
        LOCKED
    }
}
