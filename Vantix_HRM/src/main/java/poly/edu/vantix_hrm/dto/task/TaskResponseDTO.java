package poly.edu.vantix_hrm.dto.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix_hrm.entity.TaskStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponseDTO {
    private Integer taskId;
    private String taskTitle;
    private String description;
    private Integer difficultyLevel;
    private Integer urgencyLevel;
    private Integer point;
    private TaskStatus status;
    private String fileUrl;      // Thứ tự số 8
    private Integer employee_id;  // Thứ tự số 9
    private String employeeName; // Thứ tự số 10
}
