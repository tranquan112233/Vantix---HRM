package poly.edu.vantix_hrm.dto.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix_hrm.entity.TaskStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {
    private Long taskId;
    private String taskTitle;
    private String description; // Mô tả gốc của Admin
    private Integer difficultyLevel;
    private Integer urgencyLevel;
    private Integer point;
    private TaskStatus status;
    private String fileUrl;
    private Long employeeId;
    private String employeeName;

    // 🔥 Dữ liệu lấy từ bảng Assignment & Report
    private Integer progressPercent;
    private String workDescription; // Lời nhắn/mô tả của nhân viên khi nộp bài
    private LocalDateTime createdAt;
}