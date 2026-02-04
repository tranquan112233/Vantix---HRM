package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "Employees") // Công dụng: Hồ sơ nhân viên (bảng trung tâm)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employees {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Integer employeeId; // ID nhân viên

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private Users user; // Liên kết tài khoản đăng nhập

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName; // Họ và tên

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender = Gender.OTHER; // Giới tính

    @Column(name = "birth_date")
    private LocalDate birthDate; // Ngày sinh

    @Column(name = "phone", length = 20)
    private String phone; // Số điện thoại

    @Column(name = "address", length = 255)
    private String address; // Địa chỉ

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Departments department; // Phòng ban

    @ManyToOne
    @JoinColumn(name = "position_id")
    private Positions position; // Chức vụ

    @Enumerated(EnumType.STRING)
    @Column(name = "work_status")
    private WorkStatus workStatus = WorkStatus.WORKING; // Trạng thái làm việc

    public enum Gender { MALE, FEMALE, OTHER }
    public enum WorkStatus { WORKING, RESIGNED }
}