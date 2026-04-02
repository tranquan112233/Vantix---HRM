package poly.edu.vantix_hrm.dto.salaries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseSalaryTableDTO {

    private Integer id;                  // ID bảng lương
    private Integer employeeId;          // Mã ID của nhân viên
    private String employeeName;         // Tên nhân viên
    private String department;           // Tên phòng ban
    private LocalDate salaryMonth;       // Tháng tính lương
    private Integer baseSalarySnapshot;  // Lương cơ bản tại thời điểm chốt lương
    private Integer standardWorkDays;    // Số ngày công chuẩn của tháng
    private Integer actualWorkDays;      // Số ngày công thực tế đi làm
    private Integer allowance;           // Tiền phụ cấp
    private Integer bonus;               // Tiền thưởng
    private Integer bhxhAmount;          // Tiền khấu trừ Bảo hiểm xã hội (BHXH)
    private Integer bhytAmount;          // Tiền khấu trừ Bảo hiểm y tế (BHYT)
    private Integer bhtnAmount;          // Tiền khấu trừ Bảo hiểm thất nghiệp (BHTN)
    private Integer taxAmount;           // Tiền khấu trừ Thuế Thu nhập cá nhân (TNCN)
    private Integer totalIncome;         // Tổng thu nhập (Lương + Phụ cấp + Thưởng)
    private Integer totalDeduction;      // Tổng khấu trừ (Các loại BH + Thuế)
    private Integer netSalary;           // Lương thực nhận (Net Salary)
    private String status;               // Trạng thái bảng lương (DRAFT, PENDING, APPROVED, PAID)
    private String note;                 // Ghi chú thêm
}