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
@Table(name = "payroll_batches") // Quản lý các đợt/kỳ chốt lương hàng tháng
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Integer batchId;

    @Column(name = "batch_name", nullable = false)
    private String batchName; // Tên đợt (VD: Bảng lương tháng 03/2026)

    @Column(name = "salary_month", nullable = false)
    private LocalDate salaryMonth; // Lưu tháng/năm của kỳ lương này

    @Column(name = "total_employees")
    private Integer totalEmployees; // Tổng số nhân viên trong đợt này

    @Column(name = "total_net_amount", precision = 18, scale = 2)
    private BigDecimal totalNetAmount; // Tổng quỹ lương phải trả (Con số sếp nhìn)

    @Column(name = "approved_by")
    private Integer approvedBy; // ID của Giám đốc/Người duyệt

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BatchStatus status = BatchStatus.PENDING; // Trạng thái của cả đợt

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum BatchStatus {
        PENDING,  // Kế toán vừa tạo, chờ sếp duyệt
        APPROVED, // Sếp đã duyệt, kế toán chuẩn bị tiền
        REJECTED, // Sếp từ chối (bắt tính lại)
        COMPLETED // Đã chuyển khoản xong
    }
}