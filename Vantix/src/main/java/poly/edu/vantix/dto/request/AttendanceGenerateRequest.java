package poly.edu.vantix.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AttendanceGenerateRequest {

    private LocalDate fromDate;

    private LocalDate toDate;

    private Long employeeId;

    private Long departmentId;
}
