package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.service.AttendanceManagementService;

@RestController
@RequestMapping("/api/attendance-management")
@CrossOrigin("*") // Cho phép Vue gọi API
@RequiredArgsConstructor
public class AttendanceManagementController {

    private final AttendanceManagementService attendanceManagementService;

    // API: Lấy danh sách cần duyệt của phòng ban
    @GetMapping("/rejected/{managerId}")
    public ResponseEntity<?> getRejectedAttendances(@PathVariable Integer managerId) {
        try {
            return ResponseEntity.ok(attendanceManagementService.getRejectedAttendancesForManager(managerId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API: Phê duyệt chấm công
    @PutMapping("/approve/{attendanceId}")
    public ResponseEntity<?> approveAttendance(@PathVariable Integer attendanceId) {
        try {
            attendanceManagementService.approveAttendance(attendanceId);
            return ResponseEntity.ok("Phê duyệt thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}