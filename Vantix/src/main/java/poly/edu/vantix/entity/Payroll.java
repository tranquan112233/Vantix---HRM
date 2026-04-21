package poly.edu.vantix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.PayrollStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * Payroll - Phiếu lương chi tiết của một nhân viên trong một kỳ
 */
@Getter
@Setter
@Entity
@Table(
        name = "payrolls",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payroll_period_employee",
                columnNames = {"period_id", "employee_id"}
        ),
        indexes = {
                @Index(name = "idx_payroll_period", columnList = "period_id"),
                @Index(name = "idx_payroll_employee", columnList = "employee_id")
        }
)
public class Payroll extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "period_id", nullable = false)
    private PayrollPeriod period;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    // === Thông tin tính lương đầu vào ===

    @Column(name = "base_salary", precision = 19, scale = 2)
    private BigDecimal baseSalary = BigDecimal.ZERO;

    @Column(name = "insurance_salary", precision = 19, scale = 2)
    private BigDecimal insuranceSalary = BigDecimal.ZERO;

    @Column(name = "standard_work_days")
    private Integer standardWorkDays = 26;

    @Column(name = "actual_work_days", precision = 7, scale = 2)
    private BigDecimal actualWorkDays = BigDecimal.ZERO;

    @Column(name = "paid_leave_days", precision = 7, scale = 2)
    private BigDecimal paidLeaveDays = BigDecimal.ZERO;

    @Column(name = "unpaid_leave_days", precision = 7, scale = 2)
    private BigDecimal unpaidLeaveDays = BigDecimal.ZERO;

    @Column(name = "overtime_hours_weekday", precision = 7, scale = 2)
    private BigDecimal overtimeHoursWeekday = BigDecimal.ZERO;

    @Column(name = "overtime_hours_weekend", precision = 7, scale = 2)
    private BigDecimal overtimeHoursWeekend = BigDecimal.ZERO;

    @Column(name = "overtime_hours_holiday", precision = 7, scale = 2)
    private BigDecimal overtimeHoursHoliday = BigDecimal.ZERO;

    @Column(name = "overtime_hours_night", precision = 7, scale = 2)
    private BigDecimal overtimeHoursNight = BigDecimal.ZERO;

    @Column(name = "dependents")
    private Integer dependents = 0;

    // === Phụ cấp ===
    @Column(name = "responsibility_allowance", precision = 19, scale = 2)
    private BigDecimal responsibilityAllowance = BigDecimal.ZERO;

    @Column(name = "meal_allowance", precision = 19, scale = 2)
    private BigDecimal mealAllowance = BigDecimal.ZERO;

    @Column(name = "transport_allowance", precision = 19, scale = 2)
    private BigDecimal transportAllowance = BigDecimal.ZERO;

    @Column(name = "phone_allowance", precision = 19, scale = 2)
    private BigDecimal phoneAllowance = BigDecimal.ZERO;

    @Column(name = "other_allowance", precision = 19, scale = 2)
    private BigDecimal otherAllowance = BigDecimal.ZERO;

    @Column(name = "bonus", precision = 19, scale = 2)
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(name = "commission", precision = 19, scale = 2)
    private BigDecimal commission = BigDecimal.ZERO;

    // === Kết quả tính ===
    @Column(name = "working_days_salary", precision = 19, scale = 2)
    private BigDecimal workingDaysSalary = BigDecimal.ZERO;

    @Column(name = "overtime_pay", precision = 19, scale = 2)
    private BigDecimal overtimePay = BigDecimal.ZERO;

    @Column(name = "total_allowance", precision = 19, scale = 2)
    private BigDecimal totalAllowance = BigDecimal.ZERO;

    @Column(name = "gross_income", precision = 19, scale = 2)
    private BigDecimal grossIncome = BigDecimal.ZERO;

    // === Khấu trừ BHXH/BHYT/BHTN ===
    @Column(name = "social_insurance", precision = 19, scale = 2)
    private BigDecimal socialInsurance = BigDecimal.ZERO;

    @Column(name = "health_insurance", precision = 19, scale = 2)
    private BigDecimal healthInsurance = BigDecimal.ZERO;

    @Column(name = "unemployment_insurance", precision = 19, scale = 2)
    private BigDecimal unemploymentInsurance = BigDecimal.ZERO;

    @Column(name = "total_employee_insurance", precision = 19, scale = 2)
    private BigDecimal totalEmployeeInsurance = BigDecimal.ZERO;

    // === Thuế TNCN ===
    @Column(name = "taxable_income", precision = 19, scale = 2)
    private BigDecimal taxableIncome = BigDecimal.ZERO;

    @Column(name = "personal_income_tax", precision = 19, scale = 2)
    private BigDecimal personalIncomeTax = BigDecimal.ZERO;

    @Column(name = "other_deductions", precision = 19, scale = 2)
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    // === Lương thực lĩnh ===
    @Column(name = "net_income", precision = 19, scale = 2)
    private BigDecimal netIncome = BigDecimal.ZERO;

    // === Phía người sử dụng lao động ===
    @Column(name = "employer_insurance", precision = 19, scale = 2)
    private BigDecimal employerInsurance = BigDecimal.ZERO;

    @Column(name = "total_employer_cost", precision = 19, scale = 2)
    private BigDecimal totalEmployerCost = BigDecimal.ZERO;

    // Ghi chú riêng từng dòng
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Trạng thái của dòng (có thể bị chặn riêng)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayrollStatus status = PayrollStatus.DRAFT;

    // Thời gian chi trả
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
