package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "salaries") // Công dụng: Quản lý bảng lương nhân viên
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salary_id")
    private Integer salaryId; // ID bảng lương

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee; // Nhân viên

    @Column(name = "salary_month", nullable = false)
    private LocalDate salaryMonth; // Tháng lương

    @Column(name = "base_salary_snapshot", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalarySnapshot; // Lương cơ bản tại thời điểm chốt

    @Column(name = "standard_work_days", nullable = false)
    private Integer standardWorkDays; // Số ngày công chuẩn

    @Column(name = "actual_work_days", nullable = false)
    private Integer actualWorkDays; // Số ngày công thực tế

    @Column(name = "allowance", precision = 18, scale = 2)
    private BigDecimal allowance; // Phụ cấp

    @Column(name = "bonus", precision = 18, scale = 2)
    private BigDecimal bonus; // Thưởng

    @Column(name = "bhxh_amount", precision = 18, scale = 2)
    private BigDecimal bhxhAmount; // Khấu trừ BHXH

    @Column(name = "bhyt_amount", precision = 18, scale = 2)
    private BigDecimal bhytAmount; // Khấu trừ BHYT

    @Column(name = "bhtn_amount", precision = 18, scale = 2)
    private BigDecimal bhtnAmount; // Khấu trừ BHTN

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount; // Khấu trừ Thuế TNCN

    @Column(name = "total_income", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalIncome; // Tổng thu nhập

    @Column(name = "total_deduction", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalDeduction; // Tổng khấu trừ

    @Column(name = "net_salary", nullable = false, precision = 18, scale = 2)
    private BigDecimal netSalary; // Lương thực nhận

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SalaryStatus status = SalaryStatus.DRAFT; // Trạng thái bảng lương

    @Column(name = "note", columnDefinition = "TEXT")
    private String note; // Ghi chú

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt; // Thời gian tạo

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt; // Thời gian cập nhật cuối

    public enum SalaryStatus {
        DRAFT, PENDING, APPROVED
    }
}