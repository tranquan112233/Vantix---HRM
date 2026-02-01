package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.Users;
import poly.edu.vantix_hrm.service.AttendanceService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/attendance")
@CrossOrigin("*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/create")
    public Attendance save(@RequestBody Integer UserID) {
        return attendanceService.postCheckIn(UserID);
    }

    @GetMapping("/getAttendance")
    public ResponseEntity<List<Attendance>> getMonthlyAttendance(
            @RequestParam("UserID") Integer UserID,
            @RequestParam("Month") int Month,
            @RequestParam("Year") int Year
    ) {
        LocalDate dateInput = LocalDate.of(Year, Month, 1);
        List<Attendance> result = attendanceService.getEmployeeMonthlyAttendance(UserID, dateInput);
        return ResponseEntity.ok(result);
    }

}
