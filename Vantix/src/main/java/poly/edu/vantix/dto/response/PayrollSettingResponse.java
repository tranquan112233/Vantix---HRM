package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.PayrollSetting;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollSettingResponse {

    private Long id;
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
    private LocalDateTime updatedAt;

    public static PayrollSettingResponse fromEntity(PayrollSetting s) {
        return PayrollSettingResponse.builder()
                .id(s.getId())
                .employeeSocialInsuranceRate(s.getEmployeeSocialInsuranceRate())
                .employeeHealthInsuranceRate(s.getEmployeeHealthInsuranceRate())
                .employeeUnemploymentInsuranceRate(s.getEmployeeUnemploymentInsuranceRate())
                .employerSocialInsuranceRate(s.getEmployerSocialInsuranceRate())
                .employerHealthInsuranceRate(s.getEmployerHealthInsuranceRate())
                .employerUnemploymentInsuranceRate(s.getEmployerUnemploymentInsuranceRate())
                .governmentBaseSalary(s.getGovernmentBaseSalary())
                .minRegionalSalary(s.getMinRegionalSalary())
                .personalDeduction(s.getPersonalDeduction())
                .dependentDeduction(s.getDependentDeduction())
                .mealAllowanceExempt(s.getMealAllowanceExempt())
                .overtimeWeekdayMultiplier(s.getOvertimeWeekdayMultiplier())
                .overtimeWeekendMultiplier(s.getOvertimeWeekendMultiplier())
                .overtimeHolidayMultiplier(s.getOvertimeHolidayMultiplier())
                .overtimeNightMultiplier(s.getOvertimeNightMultiplier())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
