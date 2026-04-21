package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.PayrollPeriod;
import poly.edu.vantix.entity.enums.PayrollStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollPeriodResponse {

    private Long id;
    private Integer year;
    private Integer month;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer standardWorkDays;
    private String note;
    private PayrollStatus status;
    private Long approvedByUserId;
    private String approvedByUsername;
    private LocalDateTime approvedAt;
    private LocalDateTime lockedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PayrollPeriodResponse fromEntity(PayrollPeriod p) {
        return PayrollPeriodResponse.builder()
                .id(p.getId())
                .year(p.getYear())
                .month(p.getMonth())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .standardWorkDays(p.getStandardWorkDays())
                .note(p.getNote())
                .status(p.getStatus())
                .approvedByUserId(p.getApprovedBy() != null ? p.getApprovedBy().getId() : null)
                .approvedByUsername(p.getApprovedBy() != null ? p.getApprovedBy().getUsername() : null)
                .approvedAt(p.getApprovedAt())
                .lockedAt(p.getLockedAt())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
