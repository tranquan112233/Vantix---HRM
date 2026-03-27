package poly.edu.vantix_hrm.dto.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix_hrm.entity.Contract;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractResponseDTO {

    private Integer contractId;

    private Integer employeeId;
    
    private String fullName;

    private Contract.Type type;

    private LocalDate startDate;

    private LocalDate endDate;

    private String position;

    private BigDecimal baseSalary;

    private Contract.ContractStatus status;
}