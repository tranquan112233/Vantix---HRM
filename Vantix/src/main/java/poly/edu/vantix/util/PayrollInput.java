package poly.edu.vantix.util;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PayrollInput {

    // Lương chính trong hợp đồng
    private BigDecimal baseSalary;

    // Lương căn cứ đóng BHXH (nếu null sẽ lấy baseSalary)
    private BigDecimal insuranceSalary;

    // Ngày công chuẩn tháng (mặc định 26)
    private int standardWorkDays;

    // Số giờ làm/ngày (mặc định 8)
    private BigDecimal hoursPerDay;

    // Số ngày công thực tế
    private BigDecimal actualWorkDays;

    // Số ngày nghỉ có lương (phép năm, nghỉ lễ)
    private BigDecimal paidLeaveDays;

    private BigDecimal unpaidLeaveDays;

    // Giờ tăng ca các loại
    private BigDecimal overtimeHoursWeekday;
    private BigDecimal overtimeHoursWeekend;
    private BigDecimal overtimeHoursHoliday;
    private BigDecimal overtimeHoursNight;

    // Phụ cấp
    private BigDecimal responsibilityAllowance;
    private BigDecimal mealAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal phoneAllowance;
    private BigDecimal otherAllowance;

    // Thu nhập thêm
    private BigDecimal bonus;
    private BigDecimal commission;

    // Khấu trừ khác (tạm ứng, phạt)
    private BigDecimal otherDeductions;

    // Số người phụ thuộc (để giảm trừ gia cảnh)
    private Integer dependents;

    // Chính sách lương hiện hành, nếu null dùng cấu hình mặc định
    private PayrollPolicy policy;
}
