package poly.edu.vantix_hrm.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import poly.edu.vantix_hrm.dao.AttendanceDAO;
import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.service.AttendanceService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    @Autowired
    private AttendanceDAO attendanceDAO;

    @Autowired
    private AttendanceService attendanceService;

    @Scheduled(cron = "0 * * * * ?")
    public void autoCheckOutScanner() {
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        List<Attendance> listNeedAutoCheckOut = attendanceDAO.findLateEmployees(today, now);
        if (listNeedAutoCheckOut.isEmpty()) return;
        for (Attendance att : listNeedAutoCheckOut) {
            try {
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
        List<Attendance> expiredList = attendanceDAO.findExpiredPending(today, cutoffTime);
        if (expiredList.isEmpty()) return;
        for (Attendance att : expiredList) {
            try {
                att.setStatus(Attendance.AttendanceStatus.REJECTED);
                attendanceDAO.save(att);
                System.out.println("❌ Auto-Reject NV: " + att.getEmployee().getEmployeeId());
            } catch (Exception e) {
                System.err.println("Lỗi xử lý NV " + att.getEmployee().getEmployeeId() + ": " + e.getMessage());
            }
        }
    }
}
