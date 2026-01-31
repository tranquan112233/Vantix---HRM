package poly.edu.vantix_hrm.entity;


import jakarta.persistence.*;
import lombok.*;

@Table(name = "Users")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Users{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Integer userID;


    // Thông tin đăng nhập
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String passwordHash;


    // Thông tin cá nhân
    @Column(nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.Other;

    @Column(unique = true, length = 100)
    private String email;

    @Column(unique = true, length = 20)
    private String phone;

    private String address;
    private String avatarUrl;

    // Trạng thái làm việc
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.Working;

    // Quan hệ khóa ngoại
    @ManyToOne
    @JoinColumn(name = "RoleID", nullable = false)
    private Roles role;

    @ManyToOne
    @JoinColumn(name = "DepartmentID", nullable = false)
    private Departments department;

    // ENUM: Gender
    enum Gender {
        Male,
        Female,
        Other
    }
    // ENUM: UserStatus
    enum UserStatus {
        Working,
        OnLeave,
        Resigned
    }
}



