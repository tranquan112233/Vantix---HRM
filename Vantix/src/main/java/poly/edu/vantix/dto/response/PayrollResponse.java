package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.Payroll;
import poly.edu.vantix.entity.enums.PayrollStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollResponse {

    private Long id;
    private Long periodId;
    private Integer periodYear;
    private Integer periodMonth;

    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private String positionName;

    private Long contractId;
    private String contractCode;

    private BigDecimal baseSalary;
    private BigDecimal insuranceSalary;

    private Integer standardWorkDays;
    private BigDecimal actualWorkDays;
    private BigDecimal paidLeaveDays;
    private BigDecimal unpaidLeaveDays;

    private BigDecimal overtimeHoursWeekday;
    private BigDecimal overtimeHoursWeekend;
    private BigDecimal overtimeHoursHoliday;
    private BigDecimal overtimeHoursNight;

    private Integer dependents;

    private BigDecimal responsibilityAllowance;
    private BigDecimal mealAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal phoneAllowance;
    private BigDecimal otherAllowance;
    private BigDecimal totalAllowance;

    private BigDecimal bonus;
    private BigDecimal commission;

    private BigDecimal workingDaysSalary;
    private BigDecimal overtimePay;
    private BigDecimal grossIncome;

    private BigDecimal socialInsurance;
    private BigDecimal healthInsurance;
    private BigDecimal unemploymentInsurance;
    private BigDecimal totalEmployeeInsurance;

    private BigDecimal taxableIncome;
    private BigDecimal personalIncomeTax;
    private BigDecimal otherDeductions;

    private BigDecimal netIncome;

    private BigDecimal employerInsurance;
    private BigDecimal totalEmployerCost;

    private String note;
    private PayrollStatus status;
    private LocalDateTime paidAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PayrollResponse fromEntity(Payroll p) {
        return PayrollResponse.builder()
                .id(p.getId())
                .periodId(p.getPeriod() != null ? p.getPeriod().getId() : null)
                .periodYear(p.getPeriod() != null ? p.getPeriod().getYear() : null)
                .periodMonth(p.getPeriod() != null ? p.getPeriod().getMonth() : null)
                .employeeId(p.getEmployee() != null ? p.getEmployee().getId() : null)
                .employeeCode(p.getEmployee() != null ? p.getEmployee().getEmployeeCode() : null)
                .employeeName(p.getEmployee() != null ? p.getEmployee().getFullName() : null)
                .departmentName(p.getEmployee() != null && p.getEmployee().getDepartment() != null
                        ? p.getEmployee().getDepartment().getName() : null)
                .positionName(p.getEmployee() != null && p.getEmployee().getPosition() != null
                        ? p.getEmployee().getPosition().getName() : null)
                .contractId(p.getContract() != null ? p.getContract().getId() : null)
                .contractCode(p.getContract() != null ? p.getContract().getContractCode() : null)
                .baseSalary(p.getBaseSalary())
                .insuranceSalary(p.getInsuranceSalary())
                .standardWorkDays(p.getStandardWorkDays())
                .actualWorkDays(p.getActualWorkDays())
                .paidLeaveDays(p.getPaidLeaveDays())
                .unpaidLeaveDays(p.getUnpaidLeaveDays())
                .overtimeHoursWeekday(p.getOvertimeHoursWeekday())
                .overtimeHoursWeekend(p.getOvertimeHoursWeekend())
                .overtimeHoursHoliday(p.getOvertimeHoursHoliday())
                .overtimeHoursNight(p.getOvertimeHoursNight())
                .dependents(p.getDependents())
                .responsibilityAllowance(p.getResponsibilityAllowance())
                .mealAllowance(p.getMealAllowance())
                .transportAllowance(p.getTransportAllowance())
                .phoneAllowance(p.getPhoneAllowance())
                .otherAllowance(p.getOtherAllowance())
                .totalAllowance(p.getTotalAllowance())
                .bonus(p.getBonus())
                .commission(p.getCommission())
                .workingDaysSalary(p.getWorkingDaysSalary())
                .overtimePay(p.getOvertimePay())
                .grossIncome(p.getGrossIncome())
                .socialInsurance(p.getSocialInsurance())
                .healthInsurance(p.getHealthInsurance())
                .unemploymentInsurance(p.getUnemploymentInsurance())
                .totalEmployeeInsurance(p.getTotalEmployeeInsurance())
                .taxableIncome(p.getTaxableIncome())
                .personalIncomeTax(p.getPersonalIncomeTax())
                .otherDeductions(p.getOtherDeductions())
                .netIncome(p.getNetIncome())
                .employerInsurance(p.getEmployerInsurance())
                .totalEmployerCost(p.getTotalEmployerCost())
                .note(p.getNote())
                .status(p.getStatus())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
