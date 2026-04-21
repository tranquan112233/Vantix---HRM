package poly.edu.vantix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "work_schedules",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_schedule_employee_date",
                columnNames = {"employee_id", "work_date"}
        )
)
public class WorkSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private WorkLocation location;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "note", length = 255)
    private String note;
}
