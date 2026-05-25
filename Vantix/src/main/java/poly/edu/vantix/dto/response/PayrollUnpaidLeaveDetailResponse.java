package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.enums.LeaveDayUnit;
import poly.edu.vantix.entity.enums.LeaveType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollUnpaidLeaveDetailResponse {

    private Long leaveRequestId;
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveDayUnit dayUnit;
    private BigDecimal deductionDays;
    private BigDecimal deductionPercent;
    private BigDecimal deductionAmount;
    private String reason;
}
