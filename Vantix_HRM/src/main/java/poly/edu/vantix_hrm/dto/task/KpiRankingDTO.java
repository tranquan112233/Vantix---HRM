package poly.edu.vantix_hrm.dto.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiRankingDTO {
    private Long employeeId;
    private Long completedTasks;
    private Long totalPoints;
}
