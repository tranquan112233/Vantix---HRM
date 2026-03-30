package poly.edu.vantix_hrm.dto.contractannex;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ContractAnnexResponseDTO {
    private Integer contractId; // Mã hợp đồng
    private Long employeeId; // ID nhân viên
    private String employeeName; // Tên nhân viên
    private String currentPosition; // Chức vụ hiện tại (áp dụng fallback)
    private BigDecimal currentSalary; // Lương hiện tại (áp dụng fallback)

    private List<AnnexDetailDTO> annexes; // Danh sách phụ lục

    @Data
    public static class AnnexDetailDTO {
        private Integer annexId;
        private LocalDate effectiveDate;
        private BigDecimal newSalary;
        private String newPositions;
        private String content;
        private boolean isActive;
    }
}