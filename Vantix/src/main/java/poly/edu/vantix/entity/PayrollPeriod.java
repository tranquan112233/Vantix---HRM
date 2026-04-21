package poly.edu.vantix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.PayrollStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * PayrollPeriod - Kỳ lương (theo tháng)
 */
@Getter
@Setter
@Entity
@Table(
        name = "payroll_periods",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payroll_period_year_month",
                columnNames = {"period_year", "period_month"}
        )
)
public class PayrollPeriod extends BaseEntity {

    // Năm của kỳ lương
    @Column(name = "period_year", nullable = false)
    private Integer year;

    // Tháng của kỳ lương (1-12)
    @Column(name = "period_month", nullable = false)
    private Integer month;

    // Ngày bắt đầu kỳ
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // Ngày kết thúc kỳ
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // Số ngày công chuẩn của tháng
    @Column(name = "standard_work_days", nullable = false)
    private Integer standardWorkDays = 26;

    // Ghi chú
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Trạng thái kỳ lương
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayrollStatus status = PayrollStatus.DRAFT;

    // Người duyệt
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    // Thời gian duyệt
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    // Thời gian khóa (không cho tính lại)
    @Column(name = "locked_at")
    private LocalDateTime lockedAt;
}
