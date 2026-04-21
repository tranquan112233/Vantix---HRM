package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.LeaveDayUnit;
import poly.edu.vantix.entity.enums.LeaveType;

import java.time.LocalDate;

@Getter
@Setter
public class LeaveRequestRequest {

    private Long employeeId;

    @NotNull(message = "Leave type is required")
    private LeaveType type;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private LeaveDayUnit dayUnit;

    @NotBlank(message = "Reason is required")
    @Size(max = 4000, message = "Reason must be at most 4000 characters")
    private String reason;

    private Long handoverEmployeeId;

    @Size(max = 150, message = "Emergency contact must be at most 150 characters")
    private String emergencyContact;
}
