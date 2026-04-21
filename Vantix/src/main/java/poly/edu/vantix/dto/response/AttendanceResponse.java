package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.Attendance;
import poly.edu.vantix.entity.enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String departmentName;

    private Long scheduleId;
    private String shiftName;
    private LocalTime shiftStart;
    private LocalTime shiftEnd;
    private String locationName;

    private LocalDate workDate;
    private LocalDateTime checkInAt;
    private Double checkInLat;
    private Double checkInLng;
    private Double checkInDistance;

    private LocalDateTime checkOutAt;
    private Double checkOutLat;
    private Double checkOutLng;
    private Double checkOutDistance;

    private AttendanceStatus status;
    private String note;

    public static AttendanceResponse fromEntity(Attendance a) {
        AttendanceResponseBuilder builder = AttendanceResponse.builder()
                .id(a.getId())
                .workDate(a.getWorkDate())
                .checkInAt(a.getCheckInAt())
                .checkInLat(a.getCheckInLat())
                .checkInLng(a.getCheckInLng())
                .checkInDistance(a.getCheckInDistance())
                .checkOutAt(a.getCheckOutAt())
                .checkOutLat(a.getCheckOutLat())
                .checkOutLng(a.getCheckOutLng())
                .checkOutDistance(a.getCheckOutDistance())
                .status(a.getStatus())
                .note(a.getNote());

        if (a.getEmployee() != null) {
            builder.employeeId(a.getEmployee().getId())
                    .employeeCode(a.getEmployee().getEmployeeCode())
                    .employeeName(a.getEmployee().getFullName())
                    .departmentName(a.getEmployee().getDepartment() != null
                            ? a.getEmployee().getDepartment().getName()
                            : null);
        }

        if (a.getSchedule() != null) {
            builder.scheduleId(a.getSchedule().getId());
            if (a.getSchedule().getShift() != null) {
                builder.shiftName(a.getSchedule().getShift().getName())
                        .shiftStart(a.getSchedule().getShift().getStartTime())
                        .shiftEnd(a.getSchedule().getShift().getEndTime());
            }
            if (a.getSchedule().getLocation() != null) {
                builder.locationName(a.getSchedule().getLocation().getName());
            }
        }

        return builder.build();
    }
}
