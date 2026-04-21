package poly.edu.vantix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.util.PayrollPolicy;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "payroll_settings")
public class PayrollSetting extends BaseEntity {

    @Column(name = "setting_key", nullable = false, unique = true, length = 50)
    private String settingKey = "DEFAULT";

    @Column(name = "employee_social_insurance_rate", nullable = false, precision = 8, scale = 5)
    private BigDecimal employeeSocialInsuranceRate = new BigDecimal("0.08");

    @Column(name = "employee_health_insurance_rate", nullable = false, precision = 8, scale = 5)
    private BigDecimal employeeHealthInsuranceRate = new BigDecimal("0.015");

    @Column(name = "employee_unemployment_insurance_rate", nullable = false, precision = 8, scale = 5)
    private BigDecimal employeeUnemploymentInsuranceRate = new BigDecimal("0.01");

    @Column(name = "employer_social_insurance_rate", nullable = false, precision = 8, scale = 5)
    private BigDecimal employerSocialInsuranceRate = new BigDecimal("0.175");

    @Column(name = "employer_health_insurance_rate", nullable = false, precision = 8, scale = 5)
    private BigDecimal employerHealthInsuranceRate = new BigDecimal("0.03");

    @Column(name = "employer_unemployment_insurance_rate", nullable = false, precision = 8, scale = 5)
    private BigDecimal employerUnemploymentInsuranceRate = new BigDecimal("0.01");

    @Column(name = "government_base_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal governmentBaseSalary = new BigDecimal("2340000");

    @Column(name = "min_regional_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal minRegionalSalary = new BigDecimal("4960000");

    @Column(name = "personal_deduction", nullable = false, precision = 19, scale = 2)
    private BigDecimal personalDeduction = new BigDecimal("11000000");

    @Column(name = "dependent_deduction", nullable = false, precision = 19, scale = 2)
    private BigDecimal dependentDeduction = new BigDecimal("4400000");

    @Column(name = "meal_allowance_exempt", nullable = false, precision = 19, scale = 2)
    private BigDecimal mealAllowanceExempt = new BigDecimal("730000");

    @Column(name = "overtime_weekday_multiplier", nullable = false, precision = 8, scale = 3)
    private BigDecimal overtimeWeekdayMultiplier = new BigDecimal("1.5");

    @Column(name = "overtime_weekend_multiplier", nullable = false, precision = 8, scale = 3)
    private BigDecimal overtimeWeekendMultiplier = new BigDecimal("2.0");

    @Column(name = "overtime_holiday_multiplier", nullable = false, precision = 8, scale = 3)
    private BigDecimal overtimeHolidayMultiplier = new BigDecimal("3.0");

    @Column(name = "overtime_night_multiplier", nullable = false, precision = 8, scale = 3)
    private BigDecimal overtimeNightMultiplier = new BigDecimal("1.3");

    public PayrollPolicy toPolicy() {
        return PayrollPolicy.builder()
                .employeeSocialInsuranceRate(employeeSocialInsuranceRate)
                .employeeHealthInsuranceRate(employeeHealthInsuranceRate)
                .employeeUnemploymentInsuranceRate(employeeUnemploymentInsuranceRate)
                .employerSocialInsuranceRate(employerSocialInsuranceRate)
                .employerHealthInsuranceRate(employerHealthInsuranceRate)
                .employerUnemploymentInsuranceRate(employerUnemploymentInsuranceRate)
                .governmentBaseSalary(governmentBaseSalary)
                .minRegionalSalary(minRegionalSalary)
                .personalDeduction(personalDeduction)
                .dependentDeduction(dependentDeduction)
                .mealAllowanceExempt(mealAllowanceExempt)
                .overtimeWeekdayMultiplier(overtimeWeekdayMultiplier)
                .overtimeWeekendMultiplier(overtimeWeekendMultiplier)
                .overtimeHolidayMultiplier(overtimeHolidayMultiplier)
                .overtimeNightMultiplier(overtimeNightMultiplier)
                .build();
    }
}
