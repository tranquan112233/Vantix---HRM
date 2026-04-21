package poly.edu.vantix.util;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PayrollCalculation {

    private BigDecimal baseSalary;
    private BigDecimal insuranceSalary;

    private BigDecimal workingDaysSalary;
    private BigDecimal overtimePay;
    private BigDecimal allowance;
    private BigDecimal mealAllowanceExempt;
    private BigDecimal mealAllowanceTaxable;
    private BigDecimal bonus;
    private BigDecimal commission;

    private BigDecimal grossIncome;

    private BigDecimal socialInsurance;
    private BigDecimal healthInsurance;
    private BigDecimal unemploymentInsurance;
    private BigDecimal totalEmployeeInsurance;

    private BigDecimal personalDeduction;
    private BigDecimal dependentDeduction;
    private BigDecimal taxableIncome;
    private BigDecimal personalIncomeTax;

    private BigDecimal otherDeductions;

    private BigDecimal netIncome;

    private BigDecimal employerInsurance;
    private BigDecimal totalEmployerCost;
}
