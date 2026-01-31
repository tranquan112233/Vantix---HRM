package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Payroll")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PayrollID")
    private Integer payrollID;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "SalaryMonth", length = 7)
    private String salaryMonth;

    @Column(name = "BaseSalary", precision = 18, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "ActualWorkDays", precision = 4, scale = 1)
    private BigDecimal actualWorkDays;

    @Column(name = "Allowance", precision = 18, scale = 2)
    private BigDecimal allowance;

    @Column(name = "OvertimePay", precision = 18, scale = 2)
    private BigDecimal overtimePay;

    @Column(name = "Bonus", precision = 18, scale = 2)
    private BigDecimal bonus;

    @Column(name = "BhxhAmount", precision = 18, scale = 2)
    private BigDecimal bhxhAmount; // Bảo hiểm xã hội

    @Column(name = "BhytAmount", precision = 18, scale = 2)
    private BigDecimal bhytAmount; // Bảo hiểm y tế

    @Column(name = "BhtnAmount", precision = 18, scale = 2)
    private BigDecimal bhtnAmount; // Bảo hiểm thất nghiệp

    @Column(name = "TaxAmount", precision = 18, scale = 2)
    private BigDecimal taxAmount;  // Thuế TNCN

    @Column(name = "OtherDeduction", precision = 18, scale = 2)
    private BigDecimal otherDeduction;

    @Column(name = "TotalIncome", precision = 18, scale = 2)
    private BigDecimal totalIncome;

    @Column(name = "TotalDeduction", precision = 18, scale = 2)
    private BigDecimal totalDeduction;

    @Column(name = "NetSalary", precision = 18, scale = 2)
    private BigDecimal netSalary; // Thực lĩnh

    @Column(name = "PaidDate")
    private LocalDate paidDate;
}
