package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AttendanceID")
    private Integer attendanceID;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "ShiftID", nullable = false)
    private Shifts shift;

    @Column(name = "WorkDate", nullable = false)
    private LocalDate workDate;

    @Column(name = "CheckIn")
    private LocalTime checkIn;

    @Column(name = "CheckOut")
    private LocalTime checkOut;

    @Column(name = "WorkHours", precision = 5, scale = 2)
    private BigDecimal workHours;

    @Column(name = "LateMinutes")
    private Integer lateMinutes = 0;

    @Column(name = "EarlyLeaveMinutes")
    private Integer earlyLeaveMinutes = 0;

    @Column(name = "Status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.Draft;

    public enum Status {
        Draft, Pending, Approved, Rejected
    }
}
