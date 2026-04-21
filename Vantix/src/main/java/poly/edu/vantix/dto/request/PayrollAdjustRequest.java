package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/*
 * PayrollAdjustRequest - điều chỉnh tham số của một dòng lương trước khi tính lại
 */
@Getter
@Setter
public class PayrollAdjustRequest {

    @DecimalMin("0.0")
    private BigDecimal actualWorkDays;

    @DecimalMin("0.0")
    private BigDecimal paidLeaveDays;

    @DecimalMin("0.0")
    private BigDecimal unpaidLeaveDays;

    @DecimalMin("0.0")
    private BigDecimal overtimeHoursWeekday;

    @DecimalMin("0.0")
    private BigDecimal overtimeHoursWeekend;

    @DecimalMin("0.0")
    private BigDecimal overtimeHoursHoliday;

    @DecimalMin("0.0")
    private BigDecimal overtimeHoursNight;

    @Min(0)
    private Integer dependents;

    @DecimalMin("0.0")
    private BigDecimal bonus;

    @DecimalMin("0.0")
    private BigDecimal commission;

    @DecimalMin("0.0")
    private BigDecimal otherDeductions;

    @DecimalMin("0.0")
    private BigDecimal responsibilityAllowance;

    @DecimalMin("0.0")
    private BigDecimal mealAllowance;

    @DecimalMin("0.0")
    private BigDecimal transportAllowance;

    @DecimalMin("0.0")
    private BigDecimal phoneAllowance;

    @DecimalMin("0.0")
    private BigDecimal otherAllowance;

    private String note;
}
