package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PayrollSettingRequest {

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal employeeSocialInsuranceRate;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal employeeHealthInsuranceRate;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal employeeUnemploymentInsuranceRate;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal employerSocialInsuranceRate;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal employerHealthInsuranceRate;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal employerUnemploymentInsuranceRate;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal governmentBaseSalary;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal minRegionalSalary;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal personalDeduction;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal dependentDeduction;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal mealAllowanceExempt;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal overtimeWeekdayMultiplier;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal overtimeWeekendMultiplier;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal overtimeHolidayMultiplier;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal overtimeNightMultiplier;
}
