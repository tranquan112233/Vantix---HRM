package poly.edu.vantix.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.EmploymentStatus;
import poly.edu.vantix.entity.enums.Gender;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
 * Employee
 * - Hồ sơ nhân viên chính trong hệ thống HRM
 * - Tách riêng với User để phân biệt tài khoản và hồ sơ nhân sự
 */
@Getter
@Setter
@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {

    // Mã nhân viên duy nhất
    @Column(name = "employee_code", nullable = false, unique = true, length = 30)
    private String employeeCode;

    // Liên kết đến tài khoản đăng nhập, có thể null nếu chưa cấp account
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // Họ và tên nhân viên
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    // Ngày sinh
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // Giới tính
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    // Số CCCD
    @Column(name = "citizen_id", unique = true, length = 20)
    private String citizenId;

    // Số điện thoại
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // Email cá nhân
    @Column(name = "personal_email", length = 100)
    private String personalEmail;

    @Column(name = "photo_file_name", length = 255)
    private String photoFileName;

    @Column(name = "photo_original_file_name", length = 255)
    private String photoOriginalFileName;

    @Column(name = "photo_content_type", length = 120)
    private String photoContentType;

    @Column(name = "photo_file_size")
    private Long photoFileSize;

    // Địa chỉ
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    // Phòng ban làm việc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // Chức vụ hiện tại
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    // Ngày vào làm
    @Column(name = "join_date")
    private LocalDate joinDate;

    // Ngày nghỉ việc
    @Column(name = "termination_date")
    private LocalDate terminationDate;

    // Trạng thái làm việc
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", length = 30)
    private EmploymentStatus status = EmploymentStatus.ACTIVE;

    // Số tài khoản ngân hàng
    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    // Mã số thuế cá nhân
    @Column(name = "tax_code", length = 30)
    private String taxCode;

    // Số bảo hiểm
    @Column(name = "insurance_number", length = 30)
    private String insuranceNumber;

    // Tên người liên hệ khẩn cấp
    @Column(name = "emergency_contact_name", length = 150)
    private String emergencyContactName;

    // Số điện thoại liên hệ khẩn cấp
    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<EmployeeDocument> documents = new ArrayList<>();
}
