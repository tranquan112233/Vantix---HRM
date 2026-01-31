package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Table(name = "Users")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Integer userID;

    @Column(name = "Username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "Password", nullable = false, length = 200)
    private String password;

    @Column(name = "EmployeeCode", nullable = false, unique = true, length = 50)
    private String employeeCode;

    @Column(name = "Fullname", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "Gender")
    private Gender gender = Gender.Other;

    @Column(name = "Email", unique = true, length = 100)
    private String email;

    @Column(name = "Phone", unique = true, length = 20)
    private String phone;

    @Column(name = "Address", length = 255)
    private String address;

    @Column(name = "AvatarURL", length = 255)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private UserStatus status = UserStatus.Working;

    @ManyToOne
    @JoinColumn(name = "RoleID")
    private Roles role;

    @ManyToOne
    @JoinColumn(name = "DepartmentID")
    private Departments department;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    public enum Gender {
        Male, Female, Other
    }

    public enum UserStatus {
        Working, Resigned, OnLeave
    }
}