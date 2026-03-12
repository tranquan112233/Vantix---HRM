package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Daily_Work_Schedules") // Công dụng: Lưu lịch làm việc chi tiết cho từng ngày (Bảng con)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyWorkSchedules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_schedule_id")
    private Integer dailyScheduleId;

    @ManyToOne
    @JoinColumn(name = "monthly_schedule_id", nullable = false)
    private MonthlySchedules monthlySchedule; // Liên kết tới lịch của tháng

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate; // Ngày làm việc cụ thể

    @ManyToOne
    @JoinColumn(name = "shift_id")
    private Shift shift; // Liên kết tới ca làm (Cập nhật thành Shift cho đồng bộ với Employee)

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type")
    private DayType dayType; // Loại ngày làm việc

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public enum DayType { WORK, HOLIDAY, OFF, COMPENSATORY }
}