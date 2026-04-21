package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class WorkScheduleRequest {

    @NotNull(message = "Employee is required")
    private Long employeeId;

    @NotNull(message = "Shift is required")
    private Long shiftId;

    private Long locationId;

    @NotNull(message = "Work date is required")
    private LocalDate workDate;

    @Size(max = 255)
    private String note;
}
