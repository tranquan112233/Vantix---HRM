package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Task_Reports")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId;

    private Integer taskId;

    private Integer employeeId;

    private LocalDate reportDate;

    @Column(columnDefinition = "TEXT")
    private String workDescription;

    private Integer progressPercent;

    private LocalDateTime createdAt;
}