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
    @Column(name = "Username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "PasswordHash", nullable = false, length = 255)
    private String passwordHash;


    // Thông tin cá nhân
    @Column(name = "Fullname", nullable = false, length = 100)
    private String fullName;

    @Column(name = "Gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "Email", unique = true, length = 100)
    private String email;

    @Column(name = "Phone", unique = true, length = 20)
    private String phone;

    @Column(name = "Address")
    private String address;

    // Trạng thái làm việc
    @Column(name = "Status")
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



