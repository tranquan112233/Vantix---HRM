package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Monthly_Schedules") // Công dụng: Lưu thông tin lịch làm việc tổng quát theo tháng (Bảng cha)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySchedules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_schedule_id")
    private Integer monthlyScheduleId; // ID lịch tháng

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee; // Nhân viên

    @Column(name = "month", nullable = false)
    private Integer month; // Tháng (1-12)

    @Column(name = "year", nullable = false)
    private Integer year; // Năm

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ScheduleStatus status; // Trạng thái chốt lịch

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt; // Thời gian tạo

    public enum ScheduleStatus { OPEN, LOCKED }
}