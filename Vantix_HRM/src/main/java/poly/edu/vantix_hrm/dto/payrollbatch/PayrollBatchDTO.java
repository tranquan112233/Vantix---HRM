package poly.edu.vantix_hrm.dto.payrollbatch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollBatchDTO {
    private Integer batchId;
    private String batchName;
    private LocalDate salaryMonth;
    private Integer totalEmployees;
    private BigDecimal totalNetAmount;
    private Integer approvedBy;
    private String status; // Trả về String thay vì Enum để Vue dễ đọc
    private LocalDateTime createdAt;
}
