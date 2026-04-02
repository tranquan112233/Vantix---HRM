package poly.edu.vantix_hrm.dto.contractannex;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractAnnexRequestDTO {
    private Long contractId; // ID hợp đồng gốc
    private LocalDate effectiveDate; // Ngày hiệu lực
    private BigDecimal newSalary; // Lương mới (có thể null)
    private String newPositions; // Chức vụ mới (có thể null)
    private String content; // Nội dung thay đổi
    private boolean isActive; // Trạng thái áp dụng
}

