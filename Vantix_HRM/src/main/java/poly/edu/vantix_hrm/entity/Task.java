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

    private Integer point;

    private Integer createdBy;

    private LocalDate startDate;

    private LocalDate dueDate;

    // Trong file Task.java
    @Column(name = "employee_id") // Nếu trong DB bác đã thêm cột này vào bảng Tasks
    private Integer employeeId;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private LocalDateTime createdAt;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    // Giữ nguyên Getter/Setter viết tay của bạn ở đây nếu Controller vẫn đỏ
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    public String getFileUrl() {
        return fileUrl;
    }

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