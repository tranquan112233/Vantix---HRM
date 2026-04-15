package poly.edu.vantix_hrm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // Import thêm cái này để cắt vòng lặp
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Attendance") // Công dụng: Chấm công hàng ngày
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long attendanceId;

    // CÚ CHỐT 1: Dùng @JsonIgnore để cắt đứt vòng lặp vô hạn.
    // Frontend không cần kéo cả cục data Employee về làm gì cho nặng.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    // CÚ CHỐT 2: Đổi LAZY thành EAGER để fix lỗi "no session".
    // Phiếu chấm công luôn luôn cần nạp tên Ca làm để hiện thị lên lịch.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shift_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "attendances"})
    private Shift shift;

    @Column(name = "check_in")
    private LocalTime checkIn;

    @Column(name = "check_out")
    private LocalTime checkOut;

    @Column(name = "late_minutes")
    private Integer lateMinutes;

    @Column(name = "early_leave_minutes")
    private Integer earlyLeaveMinutes;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status = AttendanceStatus.DRAFT;

    public enum AttendanceStatus {DRAFT, PENDING, APPROVED, REJECTED}
}