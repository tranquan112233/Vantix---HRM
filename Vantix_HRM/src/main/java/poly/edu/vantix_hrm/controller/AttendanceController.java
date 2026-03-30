package poly.edu.vantix_hrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.entity.Shift;
import poly.edu.vantix_hrm.service.AttendanceService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/attendance")
@CrossOrigin("*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/getMonthlyAttendance")
    public ResponseEntity<List<Attendance>> getMonthlyAttendance(@RequestParam("employeeId") Long employeeId, @RequestParam("Month") int Month, @RequestParam("Year") int Year) {
        LocalDate dateInput = LocalDate.of(Year, Month, 1);
        Employee employee = attendanceService.isEmployeeValid(employeeId);
        List<Attendance> result = attendanceService.getMonthlyAttendance(employee, dateInput);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/checkIn")
    public ResponseEntity<?> CheckIn(@RequestBody Long employeeId) {
        try {
            // 1. Xác thực nhân viên
            Employee employee = attendanceService.isEmployeeValid(employeeId);

            // 2. Lấy ca làm việc được phân công của nhân viên đó trong hôm nay
            Shift shift = attendanceService.getScheduledShiftForToday(employee);

            // 3. Kiểm tra xem thời gian hiện tại có hợp lệ để check-in ca này không
            attendanceService.validateCheckInTime(shift);

            // 4. Tạo phiếu chấm công và lưu vào DB
            Attendance attendance = attendanceService.createAttendanceRecord(employee, shift);

            return ResponseEntity.ok(attendance);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PutMapping("/checkOutManual")
    public ResponseEntity<?> checkOutManual(@RequestBody Long employeeId) {
        try {
            // 1. Xác thực nhân viên
            Employee employee = attendanceService.isEmployeeValid(employeeId);

            // 2. Tìm phiếu chấm công hợp lệ (Service đã tự động kiểm tra lịch làm bên trong hàm này)
            Attendance attendance = attendanceService.findAttendanceToUpdate(employee);

            // 3. Cập nhật giờ ra và tính toán thời gian
            Attendance putAttendance = attendanceService.updateAttendanceRecord(attendance, false);

            return ResponseEntity.ok(putAttendance);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PutMapping("/confirm-checkout")
    public ResponseEntity<?> confirmCheckOut(@RequestBody Long employeeId) {
        try {
            Employee employee = attendanceService.isEmployeeValid(employeeId);
            Attendance att = attendanceService.findPendingAutoCheckOut(employee);
            attendanceService.checkTimeLimitForConfirmation(att);
            Attendance attendance = attendanceService.finalizeAndApproveCheckOut(att);
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}