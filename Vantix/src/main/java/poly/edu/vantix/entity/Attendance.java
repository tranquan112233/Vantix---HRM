package poly.edu.vantix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "attendances",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attendance_employee_date",
                columnNames = {"employee_id", "work_date"}
        )
)
public class Attendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private WorkSchedule schedule;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "check_in_at")
    private LocalDateTime checkInAt;

    @Column(name = "check_in_lat")
    private Double checkInLat;

    @Column(name = "check_in_lng")
    private Double checkInLng;

    @Column(name = "check_in_distance")
    private Double checkInDistance;

    @Column(name = "check_out_at")
    private LocalDateTime checkOutAt;

    @Column(name = "check_out_lat")
    private Double checkOutLat;

    @Column(name = "check_out_lng")
    private Double checkOutLng;

    @Column(name = "check_out_distance")
    private Double checkOutDistance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AttendanceStatus status = AttendanceStatus.PENDING;

    @Column(name = "note", length = 255)
    private String note;
}
