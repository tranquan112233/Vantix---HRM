package poly.edu.vantix_hrm.dto.schedule;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyScheduleDTO {
    private Long dailyScheduleId;

    private Long monthlyScheduleId;

    private LocalDate workDate;

    private Long shiftId;

    private String shiftName;

    private String dayType;
}
