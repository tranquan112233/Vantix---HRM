package poly.edu.vantix_hrm.dto.salaries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseSalaryTableDTO {

    private Integer id;                     // ID bảng lương
    private Long employeeId;                // Mã ID của nhân viên (Sửa từ Integer -> Long)
    private String employeeName;            // Tên nhân viên
    private String department;              // Tên phòng ban
    private LocalDate salaryMonth;          // Tháng tính lương
    private BigDecimal baseSalarySnapshot;  // Lương cơ bản tại thời điểm chốt lương
    private Integer standardWorkDays;       // Số ngày công chuẩn của tháng (Giữ nguyên Integer)
    private Integer actualWorkDays;         // Số ngày công thực tế đi làm
    private BigDecimal allowance;           // Tiền phụ cấp
    private BigDecimal bonus;               // Tiền thưởng
    private BigDecimal bhxhAmount;          // Tiền khấu trừ Bảo hiểm xã hội (BHXH)
    private BigDecimal bhytAmount;          // Tiền khấu trừ Bảo hiểm y tế (BHYT)
    private BigDecimal bhtnAmount;          // Tiền khấu trừ Bảo hiểm thất nghiệp (BHTN)
    private BigDecimal taxAmount;           // Tiền khấu trừ Thuế Thu nhập cá nhân (TNCN)
    private BigDecimal totalIncome;         // Tổng thu nhập (Lương + Phụ cấp + Thưởng)
    private BigDecimal totalDeduction;      // Tổng khấu trừ (Các loại BH + Thuế)
    private BigDecimal netSalary;           // Lương thực nhận (Net Salary)

    private String status;                  // Trạng thái bảng lương (DRAFT, PENDING, APPROVED, PAID)
    private String note;                    // Ghi chú thêm

}