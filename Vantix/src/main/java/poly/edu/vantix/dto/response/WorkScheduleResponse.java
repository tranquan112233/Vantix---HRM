package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.WorkSchedule;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleResponse {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String departmentName;

    private Long shiftId;
    private String shiftCode;
    private String shiftName;
    private LocalTime shiftStart;
    private LocalTime shiftEnd;

    private Long locationId;
    private String locationName;
    private String locationAddress;
    private Double locationLatitude;
    private Double locationLongitude;
    private Integer locationRadiusMeters;

    private LocalDate workDate;
    private String note;

    public static WorkScheduleResponse fromEntity(WorkSchedule s) {
        WorkScheduleResponseBuilder builder = WorkScheduleResponse.builder()
                .id(s.getId())
                .workDate(s.getWorkDate())
                .note(s.getNote());

        if (s.getEmployee() != null) {
            builder.employeeId(s.getEmployee().getId())
                    .employeeCode(s.getEmployee().getEmployeeCode())
                    .employeeName(s.getEmployee().getFullName())
                    .departmentName(s.getEmployee().getDepartment() != null
                            ? s.getEmployee().getDepartment().getName()
                            : null);
        }

        if (s.getShift() != null) {
            builder.shiftId(s.getShift().getId())
                    .shiftCode(s.getShift().getCode())
                    .shiftName(s.getShift().getName())
                    .shiftStart(s.getShift().getStartTime())
                    .shiftEnd(s.getShift().getEndTime());
        }

        if (s.getLocation() != null) {
            builder.locationId(s.getLocation().getId())
                    .locationName(s.getLocation().getName())
                    .locationAddress(s.getLocation().getAddress())
                    .locationLatitude(s.getLocation().getLatitude())
                    .locationLongitude(s.getLocation().getLongitude())
                    .locationRadiusMeters(s.getLocation().getRadiusMeters());
        }

        return builder.build();
    }
}
