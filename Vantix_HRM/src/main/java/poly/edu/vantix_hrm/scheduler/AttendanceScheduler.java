package poly.edu.vantix_hrm.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import poly.edu.vantix_hrm.repository.AttendanceRepository;
import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.service.AttendanceService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceService attendanceService;

    @Scheduled(cron = "0 * * * * ?") // Quét mỗi phút 1 lần
    public void autoCheckOutScanner() {
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        // Tìm những phiếu chưa check-out, và đã hết giờ ca làm
        List<Attendance> listNeedAutoCheckOut = attendanceRepository.findLateEmployees(today, now);

        if (listNeedAutoCheckOut.isEmpty()) return;

        for (Attendance att : listNeedAutoCheckOut) {
            try {
                // isAuto = true -> Lưu giờ ra bằng giờ kết thúc ca, earlyLeave = 0, status = PENDING
                attendanceService.updateAttendanceRecord(att, true);
                System.out.println("✅ Auto-checkout thành công cho NV: " + att.getEmployee().getEmployeeId());
            } catch (Exception e) {
                System.err.println("❌ Lỗi xử lý NV " + att.getEmployee().getEmployeeId() + ": " + e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 * * * * ?")
    public void autoCheckOutRejected() {
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalTime cutoffTime = now.minusMinutes(15);

        List<Attendance> expiredList = attendanceRepository.findExpiredPending(today, cutoffTime);

        if (expiredList.isEmpty()) return;

        for (Attendance att : expiredList) {
            try {
                att.setStatus(Attendance.AttendanceStatus.REJECTED);
                attendanceRepository.save(att);
                System.out.println("❌ Auto-Reject NV: " + att.getEmployee().getEmployeeId());
            } catch (Exception e) {
                System.err.println("Lỗi xử lý NV " + att.getEmployee().getEmployeeId() + ": " + e.getMessage());
            }
        }
    }
}
