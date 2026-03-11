package poly.edu.vantix_hrm.dto.leave;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveRequestDTO {
    private Integer employeeId;
    private Integer leaveTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalShift;
    private String reason;
}