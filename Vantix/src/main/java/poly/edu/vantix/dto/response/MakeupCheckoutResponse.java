package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.MakeupCheckoutRequest;
import poly.edu.vantix.entity.enums.MakeupCheckoutStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakeupCheckoutResponse {

    private Long id;

    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String departmentName;

    private Long attendanceId;
    private LocalDate workDate;
    private LocalDateTime checkInAt;
    private LocalDateTime currentCheckOutAt;

    private LocalDateTime requestedCheckOutAt;
    private String reason;

    private MakeupCheckoutStatus status;
    private Long decidedByUserId;
    private String decidedByName;
    private LocalDateTime decidedAt;
    private String decisionNote;

    private LocalDateTime createdAt;

    public static MakeupCheckoutResponse fromEntity(MakeupCheckoutRequest r) {
        MakeupCheckoutResponseBuilder builder = MakeupCheckoutResponse.builder()
                .id(r.getId())
                .requestedCheckOutAt(r.getRequestedCheckOutAt())
                .reason(r.getReason())
                .status(r.getStatus())
                .decidedAt(r.getDecidedAt())
                .decisionNote(r.getDecisionNote())
                .createdAt(r.getCreatedAt());

        if (r.getEmployee() != null) {
            builder.employeeId(r.getEmployee().getId())
                    .employeeCode(r.getEmployee().getEmployeeCode())
                    .employeeName(r.getEmployee().getFullName())
                    .departmentName(r.getEmployee().getDepartment() != null
                            ? r.getEmployee().getDepartment().getName()
                            : null);
        }

        if (r.getAttendance() != null) {
            builder.attendanceId(r.getAttendance().getId())
                    .workDate(r.getAttendance().getWorkDate())
                    .checkInAt(r.getAttendance().getCheckInAt())
                    .currentCheckOutAt(r.getAttendance().getCheckOutAt());
        }

        if (r.getDecidedBy() != null) {
            builder.decidedByUserId(r.getDecidedBy().getId())
                    .decidedByName(r.getDecidedBy().getUsername());
        }

        return builder.build();
    }
}
