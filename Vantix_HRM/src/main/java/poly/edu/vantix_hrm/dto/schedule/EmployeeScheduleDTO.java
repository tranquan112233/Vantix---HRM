package poly.edu.vantix_hrm.dto.schedule;

import lombok.Data;

@Data
public class EmployeeScheduleDTO {
    private Long employeeId;
    private String fullName;
    private boolean manager;

    private MonthlyScheduleDTO monthlySchedule;
}
