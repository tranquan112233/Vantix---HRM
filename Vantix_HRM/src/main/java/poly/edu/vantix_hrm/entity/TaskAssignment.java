package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Task_Assignments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer assignmentId;

    private Integer taskId;

    private Integer employeeId;

    private LocalDateTime assignedAt;

}