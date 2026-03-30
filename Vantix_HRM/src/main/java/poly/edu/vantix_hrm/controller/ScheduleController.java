package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.schedule.DailyScheduleDTO;
import poly.edu.vantix_hrm.dto.schedule.EmployeeScheduleDTO;
import poly.edu.vantix_hrm.service.ScheduleService;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<EmployeeScheduleDTO>> getSchedules(@RequestParam Long viewerId, @RequestParam int month, @RequestParam int year) {
        try {
            List<EmployeeScheduleDTO> schedules = scheduleService.getSchedulesForUser(viewerId, month, year);
            return ResponseEntity.ok(schedules);

        } catch (RuntimeException e) {
            // THÊM DÒNG NÀY VÀO: Để nó in lỗi đỏ ra màn hình console của IntelliJ
            e.printStackTrace();

            // Bạn cũng có thể trả thẳng câu thông báo lỗi về cho Vue để dễ đọc
            return ResponseEntity.status(400).body(null);
        }
    }

    @PostMapping("/{employeeId}/{month}/{year}/daily")
    public ResponseEntity<String> saveDailySchedules(@PathVariable Long employeeId, @PathVariable int month, @PathVariable int year, @RequestBody List<DailyScheduleDTO> dailySchedules) {
        try {
            scheduleService.saveDailySchedules(employeeId, month, year, dailySchedules);
            return ResponseEntity.ok("Lưu lịch thành công!");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi lưu lịch: " + e.getMessage());
        }
    }

    @PutMapping("/{monthlyScheduleId}/status")
    public ResponseEntity<String> updateScheduleStatus(@PathVariable Integer monthlyScheduleId, @RequestParam String status) {
        try {
            scheduleService.updateScheduleStatus(monthlyScheduleId, status);
            return ResponseEntity.ok("Cập nhật trạng thái thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}
