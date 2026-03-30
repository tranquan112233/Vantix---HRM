package poly.edu.vantix_hrm.dto.schedule;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyScheduleDTO {
    private Integer dailyScheduleId;

    private Integer monthlyScheduleId;

    private LocalDate workDate;

    private Integer shiftId;

    private String shiftName;

    private String dayType;
}
