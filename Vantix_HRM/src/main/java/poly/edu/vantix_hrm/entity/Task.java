package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "task")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "task_title", nullable = false)
    private String taskTitle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel;

    @Column(name = "urgency_level")
    private Integer urgencyLevel;

    @Column(name = "point")
    private Integer point;

    @Column(name = "file_url")
    private String fileUrl;

    // Lưu ID nhân viên được giao việc (không map ManyToOne cho nhẹ)
    @Column(name = "employee_id")
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TaskStatus status;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Map với bảng Employee (người tạo task)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private Employee createdBy;
}