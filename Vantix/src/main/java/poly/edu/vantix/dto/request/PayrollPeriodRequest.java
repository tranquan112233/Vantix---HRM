package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PayrollPeriodRequest {

    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer year;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer month;

    private LocalDate startDate;

    private LocalDate endDate;

    @Min(1)
    @Max(31)
    private Integer standardWorkDays;

    @Size(max = 1000)
    private String note;
}
