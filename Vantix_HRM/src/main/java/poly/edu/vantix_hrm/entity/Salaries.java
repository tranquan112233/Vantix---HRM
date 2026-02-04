package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Salaries") // Công dụng: Bảng lương theo tháng
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Salaries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salary_id")
    private Integer salaryId; // ID bảng lương

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employees employees; // Nhân viên nhận lương

    @Column(name = "salary_month")
    private LocalDate salaryMonth; // Tháng/Năm tính lương

    @Column(name = "base_salary", precision = 18, scale = 2)
    private BigDecimal baseSalary; // Lương cơ bản

    @Column(name = "actual_shifts")
    private Integer actualShifts; // Ca đã làm

    @Column(name = "allowance", precision = 18, scale = 2)
    private BigDecimal allowance; // Phụ cấp

    @Column(name = "overtime_pay", precision = 18, scale = 2)
    private BigDecimal overtimePay; // Tăng ca (DB đang sai chính tả oivertime)

    @Column(name = "bonus", precision = 18, scale = 2)
    private BigDecimal bonus; // Thưởng

    @Column(name = "bhxh_amount", precision = 18, scale = 2)
    private BigDecimal bhxhAmount; // Bảo hiểm xã hội

    @Column(name = "bhyt_amount", precision = 18, scale = 2)
    private BigDecimal bhytAmount; // Bảo hiểm y tế

    @Column(name = "bhtn_amount", precision = 18, scale = 2)
    private BigDecimal bhtnAmount; // Bảo hiểm thất nghiệp

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount; // Thuế TNCN

    @Column(name = "total_income", precision = 18, scale = 2)
    private BigDecimal totalIncome; // Tổng lương tháng

    @Column(name = "total_deduction", precision = 18, scale = 2)
    private BigDecimal totalDeduction; // Tổng khấu trừ

    @Column(name = "total_salary", precision = 18, scale = 2)
    private BigDecimal totalSalary; // Tổng lương thực nhận
}