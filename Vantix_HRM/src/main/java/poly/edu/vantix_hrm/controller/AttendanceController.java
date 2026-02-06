package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.Employees;
import poly.edu.vantix_hrm.entity.Shifts;
import poly.edu.vantix_hrm.service.AttendanceService;

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

    @GetMapping("/getMonthlyAttendance")
    public ResponseEntity<List<Attendance>> getMonthlyAttendance(@RequestParam("employeeId") Integer employeeId, @RequestParam("Month") int Month, @RequestParam("Year") int Year) {
        LocalDate dateInput = LocalDate.of(Year, Month, 1);
        Employees employees = attendanceService.isEmployeeValid(employeeId);
        List<Attendance> result = attendanceService.getMonthlyAttendance(employees, dateInput);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/checkIn")
    public ResponseEntity<?> CheckIn(@RequestBody Integer employeeId) {
        try {
            LocalTime VietNam = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            Employees employee = attendanceService.isEmployeeValid(employeeId);
            Shifts shift = attendanceService.getCurrentShift();
            Attendance attendance = attendanceService.createAttendanceRecord(employee, shift);
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}