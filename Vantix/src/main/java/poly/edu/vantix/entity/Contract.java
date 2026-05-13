package poly.edu.vantix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.ContractStatus;
import poly.edu.vantix.entity.enums.ContractType;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * Contract
 * - Hợp đồng lao động giữa công ty và nhân viên
 * - Tuân thủ Bộ luật Lao động 2019 của Việt Nam
 */
@Getter
@Setter
@Entity
@Table(
        name = "contracts",
        indexes = {
                @Index(name = "idx_contract_employee", columnList = "employee_id"),
                @Index(name = "idx_contract_status", columnList = "status"),
                @Index(name = "idx_contract_code", columnList = "contract_code")
        }
)
public class Contract extends BaseEntity {

    // Mã hợp đồng (duy nhất)
    @Column(name = "contract_code", nullable = false, unique = true, length = 50)
    private String contractCode;

    // Nhân viên ký hợp đồng
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Chức vụ khi ký HĐ (lưu lại để có lịch sử, không lệ thuộc Position hiện tại)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    // Loại hợp đồng
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 30)
    private ContractType contractType = ContractType.FIXED_TERM;

    // Trạng thái hợp đồng
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContractStatus status = ContractStatus.DRAFT;

    // Ngày ký
    @Column(name = "signed_date")
    private LocalDate signedDate;

    // Ngày bắt đầu hiệu lực
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // Ngày kết thúc (null nếu INDEFINITE)
    @Column(name = "end_date")
    private LocalDate endDate;

    // Số tháng thử việc (tối đa 180 ngày, 60 ngày, 30 ngày hoặc 6 ngày theo Điều 25)
    @Column(name = "probation_months")
    private Integer probationMonths;

    // Lương chính (VND) - làm căn cứ đóng BHXH nếu insuranceSalary không khai báo riêng
    @Column(name = "base_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseSalary = BigDecimal.ZERO;

    // Lương làm căn cứ đóng BHXH (nếu khác lương chính, thường = baseSalary)
    @Column(name = "insurance_salary", precision = 19, scale = 2)
    private BigDecimal insuranceSalary;

    // Phụ cấp trách nhiệm
    @Column(name = "responsibility_allowance", precision = 19, scale = 2)
    private BigDecimal responsibilityAllowance = BigDecimal.ZERO;

    // Phụ cấp ăn trưa (được miễn thuế TNCN tối đa 730.000đ/tháng)
    @Column(name = "meal_allowance", precision = 19, scale = 2)
    private BigDecimal mealAllowance = BigDecimal.ZERO;

    // Phụ cấp đi lại (xăng xe)
    @Column(name = "transport_allowance", precision = 19, scale = 2)
    private BigDecimal transportAllowance = BigDecimal.ZERO;

    // Phụ cấp điện thoại (miễn thuế theo quy định nội bộ)
    @Column(name = "phone_allowance", precision = 19, scale = 2)
    private BigDecimal phoneAllowance = BigDecimal.ZERO;

    // Phụ cấp khác
    @Column(name = "other_allowance", precision = 19, scale = 2)
    private BigDecimal otherAllowance = BigDecimal.ZERO;

    // Số ngày công chuẩn/tháng (mặc định 26 ngày với tuần 6 buổi)
    @Column(name = "standard_work_days")
    private Integer standardWorkDays = 26;

    // Số giờ làm/ngày (mặc định 8h)
    @Column(name = "hours_per_day", precision = 4, scale = 2)
    private BigDecimal hoursPerDay = new BigDecimal("8.00");

    // Thời hạn thông báo chấm dứt (ngày) - điều 35
    @Column(name = "notice_period_days")
    private Integer noticePeriodDays;

    // Ngày chấm dứt thực tế (nếu TERMINATED)
    @Column(name = "terminated_date")
    private LocalDate terminatedDate;

    // Lý do chấm dứt
    @Column(name = "termination_reason", columnDefinition = "TEXT")
    private String terminationReason;

    // Đường dẫn tệp đính kèm (PDF đã ký)
    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;

    @Column(name = "attachment_original_file_name", length = 255)
    private String attachmentOriginalFileName;

    @Column(name = "attachment_content_type", length = 100)
    private String attachmentContentType;

    @Column(name = "attachment_file_size")
    private Long attachmentFileSize;

    // Ghi chú thêm
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
