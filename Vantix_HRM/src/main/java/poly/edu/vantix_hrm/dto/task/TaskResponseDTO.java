package poly.edu.vantix_hrm.dto.task;

import lombok.*;
import poly.edu.vantix_hrm.entity.TaskStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponseDTO {

    private Long taskId;
    private String taskTitle;
    private String description;
    private Integer difficultyLevel;
    private Integer urgencyLevel;
    private Integer point;
    private TaskStatus status;
    private String fileUrl;

    // Hai trường này dùng để lấy thông tin nhân viên mà không bị lặp đệ quy JSON
    private Long employeeId;
    private String employeeName;

}