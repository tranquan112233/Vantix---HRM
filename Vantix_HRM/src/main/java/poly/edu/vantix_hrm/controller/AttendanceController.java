package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.Shifts;
import poly.edu.vantix_hrm.entity.Users;
import poly.edu.vantix_hrm.service.AttendanceService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("api/attendance")
@CrossOrigin("*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/create")
    public ResponseEntity<?> checkIn(@RequestBody Integer userId) {
        try {
            LocalTime gioVN = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            Users users = attendanceService.checkUser(userId);
            Shifts shifts = attendanceService.checkShift();
            Integer lateMinutes = 0;
            if (gioVN.isAfter(shifts.getStartTime())) {
                lateMinutes = (int) Duration.between(shifts.getStartTime(), gioVN).toMinutes();
            }
            Attendance saveAttendance = attendanceService.postCheckIn(shifts, users, lateMinutes);
            return ResponseEntity.ok(saveAttendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("🔥 Lỗi hệ thống: " + e.getMessage());
        }
    }

    @GetMapping("/getAttendance")
    public ResponseEntity<List<Attendance>> getMonthlyAttendance(@RequestParam("UserID") Integer UserID, @RequestParam("Month") int Month, @RequestParam("Year") int Year) {
        LocalDate dateInput = LocalDate.of(Year, Month, 1);
        List<Attendance> result = attendanceService.getEmployeeMonthlyAttendance(UserID, dateInput);
        return ResponseEntity.ok(result);
    }

}
