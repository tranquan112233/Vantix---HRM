package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {  // ← Thêm extends BaseEntity

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // ← Đổi Integer → Long

    // ================= USER =================
    @OneToOne(fetch = FetchType.LAZY)  // ← Đổi EAGER → LAZY
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ================= BASIC INFO =================
    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(length = 20, nullable = false)
    private String phone;

    @Column(length = 255, nullable = false)
    private String address;

    // ================= ORGANIZATION =================
    @ManyToOne(fetch = FetchType.LAZY)  // ← Đổi EAGER → LAZY
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)  // ← Đổi EAGER → LAZY
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    // ================= STATUS =================
    @Enumerated(EnumType.STRING)
    @Column(name = "work_status", nullable = false)
    private WorkStatus workStatus;

    public enum Gender { MALE, FEMALE, OTHER }
    public enum WorkStatus { WORKING, RESIGNED }
}