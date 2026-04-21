package poly.edu.vantix.util;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PayrollPolicy {

    private BigDecimal employeeSocialInsuranceRate;
    private BigDecimal employeeHealthInsuranceRate;
    private BigDecimal employeeUnemploymentInsuranceRate;
    private BigDecimal employerSocialInsuranceRate;
    private BigDecimal employerHealthInsuranceRate;
    private BigDecimal employerUnemploymentInsuranceRate;
    private BigDecimal governmentBaseSalary;
    private BigDecimal minRegionalSalary;
    private BigDecimal personalDeduction;
    private BigDecimal dependentDeduction;
    private BigDecimal mealAllowanceExempt;
    private BigDecimal overtimeWeekdayMultiplier;
    private BigDecimal overtimeWeekendMultiplier;
    private BigDecimal overtimeHolidayMultiplier;
    private BigDecimal overtimeNightMultiplier;

    public static PayrollPolicy defaults() {
        return PayrollPolicy.builder()
                .employeeSocialInsuranceRate(new BigDecimal("0.08"))
                .employeeHealthInsuranceRate(new BigDecimal("0.015"))
                .employeeUnemploymentInsuranceRate(new BigDecimal("0.01"))
                .employerSocialInsuranceRate(new BigDecimal("0.175"))
                .employerHealthInsuranceRate(new BigDecimal("0.03"))
                .employerUnemploymentInsuranceRate(new BigDecimal("0.01"))
                .governmentBaseSalary(new BigDecimal("2340000"))
                .minRegionalSalary(new BigDecimal("4960000"))
                .personalDeduction(new BigDecimal("11000000"))
                .dependentDeduction(new BigDecimal("4400000"))
                .mealAllowanceExempt(new BigDecimal("730000"))
                .overtimeWeekdayMultiplier(new BigDecimal("1.5"))
                .overtimeWeekendMultiplier(new BigDecimal("2.0"))
                .overtimeHolidayMultiplier(new BigDecimal("3.0"))
                .overtimeNightMultiplier(new BigDecimal("1.3"))
                .build();
    }
}
