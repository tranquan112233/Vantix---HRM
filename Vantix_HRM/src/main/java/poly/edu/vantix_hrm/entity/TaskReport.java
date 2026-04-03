package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "progress_percent")
    private Integer progressPercent;

    @Column(name = "work_description", columnDefinition = "TEXT")
    private String workDescription;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}