package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.Contract;
import poly.edu.vantix.entity.enums.ContractStatus;
import poly.edu.vantix.entity.enums.ContractType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponse {

    private Long id;
    private String contractCode;

    private Long employeeId;
    private String employeeCode;
    private String employeeName;

    private Long positionId;
    private String positionName;

    private ContractType contractType;
    private ContractStatus status;

    private LocalDate signedDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer probationMonths;

    private BigDecimal baseSalary;
    private BigDecimal insuranceSalary;
    private BigDecimal responsibilityAllowance;
    private BigDecimal mealAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal phoneAllowance;
    private BigDecimal otherAllowance;
    private BigDecimal totalAllowance;
    private BigDecimal totalGrossSalary;

    private Integer standardWorkDays;
    private BigDecimal hoursPerDay;
    private Integer noticePeriodDays;

    private LocalDate terminatedDate;
    private String terminationReason;
    private String attachmentPath;
    private String signedFileName;
    private String signedFileContentType;
    private Long signedFileSize;
    private String note;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ContractResponse fromEntity(Contract c) {
        BigDecimal allowance = nz(c.getResponsibilityAllowance())
                .add(nz(c.getMealAllowance()))
                .add(nz(c.getTransportAllowance()))
                .add(nz(c.getPhoneAllowance()))
                .add(nz(c.getOtherAllowance()));

        BigDecimal gross = nz(c.getBaseSalary()).add(allowance);

        return ContractResponse.builder()
                .id(c.getId())
                .contractCode(c.getContractCode())
                .employeeId(c.getEmployee() != null ? c.getEmployee().getId() : null)
                .employeeCode(c.getEmployee() != null ? c.getEmployee().getEmployeeCode() : null)
                .employeeName(c.getEmployee() != null ? c.getEmployee().getFullName() : null)
                .positionId(c.getPosition() != null ? c.getPosition().getId() : null)
                .positionName(c.getPosition() != null ? c.getPosition().getName() : null)
                .contractType(c.getContractType())
                .status(c.getStatus())
                .signedDate(c.getSignedDate())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .probationMonths(c.getProbationMonths())
                .baseSalary(c.getBaseSalary())
                .insuranceSalary(c.getInsuranceSalary())
                .responsibilityAllowance(c.getResponsibilityAllowance())
                .mealAllowance(c.getMealAllowance())
                .transportAllowance(c.getTransportAllowance())
                .phoneAllowance(c.getPhoneAllowance())
                .otherAllowance(c.getOtherAllowance())
                .totalAllowance(allowance)
                .totalGrossSalary(gross)
                .standardWorkDays(c.getStandardWorkDays())
                .hoursPerDay(c.getHoursPerDay())
                .noticePeriodDays(c.getNoticePeriodDays())
                .terminatedDate(c.getTerminatedDate())
                .terminationReason(c.getTerminationReason())
                .attachmentPath(c.getAttachmentPath())
                .signedFileName(c.getAttachmentOriginalFileName())
                .signedFileContentType(c.getAttachmentContentType())
                .signedFileSize(c.getAttachmentFileSize())
                .note(c.getNote())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
