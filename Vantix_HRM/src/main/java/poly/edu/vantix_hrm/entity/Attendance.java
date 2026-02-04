package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Attendance") // Công dụng: Chấm công hàng ngày
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Integer attendanceId; // ID chấm công

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employees employee; // Nhân viên

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate; // Ngày làm việc

    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    private Shifts shift; // Ca làm

    @Column(name = "check_in")
    private LocalTime checkIn; // Giờ vào

    @Column(name = "check_out")
    private LocalTime checkOut; // Giờ ra

    @Column(name = "late_minutes")
    private Integer lateMinutes; // Phút trễ

    @Column(name = "early_leave_minutes")
    private Integer earlyLeaveMinutes; // Phút về sớm

    @Column(name = "status")
    private AttendanceStatus status = AttendanceStatus.DRAFT; // Trạng thái phiếu chấm công

    public enum AttendanceStatus {DRAFT, PENDING, APPROVED, REJECTED}
}