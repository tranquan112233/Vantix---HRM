package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.LeaveRequest;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.entity.enums.LeaveDayUnit;
import poly.edu.vantix.entity.enums.LeaveRequestStatus;
import poly.edu.vantix.entity.enums.LeaveType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestResponse {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private Long departmentId;
    private String departmentName;
    private Long positionId;
    private String positionName;
    private LeaveType type;
    private LeaveRequestStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveDayUnit dayUnit;
    private String reason;
    private Long handoverEmployeeId;
    private String handoverEmployeeCode;
    private String handoverTo;
    private String emergencyContact;
    private Long decidedByUserId;
    private String decidedBy;
    private LocalDateTime decidedAt;
    private String decisionNote;
    private Long createdBy;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LeaveRequestResponse fromEntity(LeaveRequest request) {
        Employee employee = request.getEmployee();
        Employee handoverEmployee = request.getHandoverEmployee();
        User decidedBy = request.getDecidedBy();

        return LeaveRequestResponse.builder()
                .id(request.getId())
                .employeeId(employee != null ? employee.getId() : null)
                .employeeCode(employee != null ? employee.getEmployeeCode() : null)
                .employeeName(employee != null ? employee.getFullName() : null)
                .departmentId(employee != null && employee.getDepartment() != null
                        ? employee.getDepartment().getId()
                        : null)
                .departmentName(employee != null && employee.getDepartment() != null
                        ? employee.getDepartment().getName()
                        : null)
                .positionId(employee != null && employee.getPosition() != null
                        ? employee.getPosition().getId()
                        : null)
                .positionName(employee != null && employee.getPosition() != null
                        ? employee.getPosition().getName()
                        : null)
                .type(request.getType())
                .status(request.getStatus())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .dayUnit(request.getDayUnit())
                .reason(request.getReason())
                .handoverEmployeeId(handoverEmployee != null ? handoverEmployee.getId() : null)
                .handoverEmployeeCode(handoverEmployee != null ? handoverEmployee.getEmployeeCode() : null)
                .handoverTo(handoverEmployee != null ? employeeLabel(handoverEmployee) : null)
                .emergencyContact(request.getEmergencyContact())
                .decidedByUserId(decidedBy != null ? decidedBy.getId() : null)
                .decidedBy(decidedBy != null ? decidedBy.getUsername() : null)
                .decidedAt(request.getDecidedAt())
                .decisionNote(request.getDecisionNote())
                .createdBy(request.getCreatedBy())
                .submittedAt(request.getCreatedAt())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    private static String employeeLabel(Employee employee) {
        String code = employee.getEmployeeCode() == null || employee.getEmployeeCode().isBlank()
                ? ""
                : " (" + employee.getEmployeeCode() + ")";
        String department = employee.getDepartment() == null
                ? ""
                : " - " + employee.getDepartment().getName();
        return employee.getFullName() + code + department;
    }
}
