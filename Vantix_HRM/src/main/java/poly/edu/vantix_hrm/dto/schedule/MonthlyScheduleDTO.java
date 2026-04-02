package poly.edu.vantix_hrm.dto.schedule;

import lombok.Data;

import java.util.List;

@Data
public class MonthlyScheduleDTO {
    private Long monthlyScheduleId;

    private Integer month;
    private Integer year;
    private String status;

    private List<DailyScheduleDTO> dailySchedules;
}
