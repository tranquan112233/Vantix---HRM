package poly.edu.vantix_hrm.dto.attendancemanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectedAttendanceDTO {
    private Integer attendanceId; // Để làm key gọi API duyệt
    private Integer employeeId;   // Mã NV
    private String fullName;      // Tên NV
    private LocalDate workDate;   // Ngày làm việc
    private Integer shiftId;
    private String shiftName;     // Ca làm
    private LocalTime checkIn;    // Giờ vào
    private LocalTime checkOut;   // Giờ ra
    private Integer lateMinutes;  // Phút đi trễ
    private Integer earlyLeaveMinutes; // Phút về sớm
    private String status;        // Trạng thái (REJECTED)
}