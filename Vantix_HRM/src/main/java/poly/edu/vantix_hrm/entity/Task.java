package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Tasks")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer taskId;

    private String taskTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer difficultyLevel;

    private Integer urgencyLevel;

    private Integer point; // 🔥 thêm để tính KPI

    private Integer createdBy;

    private LocalDate startDate;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private LocalDateTime createdAt;

    // 🔥 AUTO TÍNH POINT (chuẩn pro)
    @PrePersist
    @PreUpdate
    public void calculatePoint() {
        if (difficultyLevel != null && urgencyLevel != null) {
            this.point = difficultyLevel * urgencyLevel * 10;
        } else {
            this.point = 0;
        }
    }
}