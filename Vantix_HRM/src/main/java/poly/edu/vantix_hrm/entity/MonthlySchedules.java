package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Monthly_Schedules") // Công dụng: Lưu thông tin lịch làm việc tổng quát theo tháng (Bảng cha)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySchedules extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_schedule_id")
    private Long monthlyScheduleId; // ID lịch tháng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee; // Nhân viên

    @Column(name = "month", nullable = false)
    private Integer month; // Tháng (1-12)

    @Column(name = "year", nullable = false)
    private Integer year; // Năm

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ScheduleStatus status; // Trạng thái chốt lịch

    public enum ScheduleStatus { OPEN, LOCKED }
}