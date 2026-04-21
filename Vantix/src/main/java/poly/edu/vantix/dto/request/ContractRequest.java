package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.ContractStatus;
import poly.edu.vantix.entity.enums.ContractType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ContractRequest {

    @NotBlank(message = "Contract code is required")
    @Size(max = 50)
    private String contractCode;

    @NotNull(message = "Employee is required")
    private Long employeeId;

    private Long positionId;

    @NotNull(message = "Contract type is required")
    private ContractType contractType;

    private ContractStatus status;

    private LocalDate signedDate;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private Integer probationMonths;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", message = "Base salary must be >= 0")
    private BigDecimal baseSalary;

    @DecimalMin(value = "0.0", message = "Insurance salary must be >= 0")
    private BigDecimal insuranceSalary;

    @DecimalMin(value = "0.0")
    private BigDecimal responsibilityAllowance;

    @DecimalMin(value = "0.0")
    private BigDecimal mealAllowance;

    @DecimalMin(value = "0.0")
    private BigDecimal transportAllowance;

    @DecimalMin(value = "0.0")
    private BigDecimal phoneAllowance;

    @DecimalMin(value = "0.0")
    private BigDecimal otherAllowance;

    private Integer standardWorkDays;

    private BigDecimal hoursPerDay;

    private Integer noticePeriodDays;

    private LocalDate terminatedDate;

    @Size(max = 4000)
    private String terminationReason;

    @Size(max = 500)
    private String attachmentPath;

    @Size(max = 4000)
    private String note;
}
