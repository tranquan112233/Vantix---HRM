package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class WorkScheduleBulkRequest {

    private List<Long> employeeIds;

    private List<Long> departmentIds;

    @NotNull(message = "Shift is required")
    private Long shiftId;

    private Long locationId;

    @NotNull(message = "Start date is required")
    private LocalDate fromDate;

    @NotNull(message = "End date is required")
    private LocalDate toDate;

    @NotEmpty(message = "Select at least one weekday")
    private List<DayOfWeek> daysOfWeek;

    private Boolean skipExisting = Boolean.TRUE;

    private Boolean skipPublicHolidays = Boolean.TRUE;

    @Size(max = 255)
    private String note;
}
