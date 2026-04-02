package poly.edu.vantix_hrm.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId; // Đổi thành Long

    @Column(name = "task_title", nullable = false)
    private String taskTitle; // Khớp với Vue

    @Column(columnDefinition = "TEXT")
    private String description;

    // Các trường tính điểm theo Vue
    private Integer difficultyLevel;
    private Integer urgencyLevel;
    private Integer point;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "employee_id")
    private Long employeeId; // Đổi thành Long

    @Column(name = "file_url")
    private String fileUrl; // Cho phép lưu link Cloudinary

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user", "department", "position"})
    private Employee createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) this.status = TaskStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }
}