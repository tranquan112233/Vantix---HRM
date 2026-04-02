package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.service.AttendanceManagementService;

@RestController
@RequestMapping("/api/attendance-management")
@CrossOrigin("*") // Cho phép Vue gọi API
@RequiredArgsConstructor
public class AttendanceManagementController {

    private final AttendanceManagementService attendanceManagementService;

    // API: Lấy danh sách chấm công PENDING của phòng ban cần duyệt
    @PreAuthorize("hasAuthority('ATTENDANCE_MANAGEMENT_VIEW')")
    @GetMapping("/pending/{managerId}")
    public ResponseEntity<?> getPendingAttendances(@PathVariable Long managerId) {
        try {
            return ResponseEntity.ok(attendanceManagementService.getPendingAttendancesForManager(managerId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API: Phê duyệt chấm công
    @PreAuthorize("hasAuthority('ATTENDANCE_MANAGEMENT_APPROVE')")
    @PutMapping("/approve/{attendanceId}")
    public ResponseEntity<?> approveAttendance(@PathVariable Long attendanceId) {
        try {
            attendanceManagementService.approveAttendance(attendanceId);
            return ResponseEntity.ok("Phê duyệt thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API: Từ chối chấm công
    @PreAuthorize("hasAuthority('ATTENDANCE_MANAGEMENT_APPROVE')")
    @PutMapping("/reject/{attendanceId}")
    public ResponseEntity<?> rejectAttendance(@PathVariable Long attendanceId) {
        try {
            attendanceManagementService.rejectAttendance(attendanceId);
            return ResponseEntity.ok("Từ chối chấm công thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

