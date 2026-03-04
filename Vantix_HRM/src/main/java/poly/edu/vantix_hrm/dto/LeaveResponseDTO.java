package poly.edu.vantix_hrm.DTO;

import lombok.Data;
import poly.edu.vantix_hrm.entity.LeaveStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveResponseDTO {
    private Integer leaveId;
    private String employeeName;
    private String leaveTypeName;
    private Boolean isPaid;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalShift;
    private String reason;
    private LeaveStatus status;
    private String approvedByName;
    private LocalDateTime createdAt;
}