package poly.edu.vantix_hrm.dto.schedule;

import lombok.Data;

@Data
public class EmployeeScheduleDTO {
    private Integer employeeId;
    private String fullName;
    
    private MonthlyScheduleDTO monthlySchedule;
}
