package poly.edu.vantix.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.entity.Attendance;
import poly.edu.vantix.entity.Shift;
import poly.edu.vantix.entity.WorkSchedule;
import poly.edu.vantix.entity.enums.AttendanceStatus;
import poly.edu.vantix.entity.enums.NotificationType;
import poly.edu.vantix.repository.AttendanceRepository;

import java.time.LocalDate;
import java.util.List;

@Component
public class AttendanceAutoCloseJob {

    private static final Logger log = LoggerFactory.getLogger(AttendanceAutoCloseJob.class);

    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;

    public AttendanceAutoCloseJob(
            AttendanceRepository attendanceRepository,
            NotificationService notificationService
    ) {
        this.attendanceRepository = attendanceRepository;
        this.notificationService = notificationService;
    }

    // Chạy lúc 23:55 mỗi ngày (giờ VN). Đánh dấu các bản ghi check-in nhưng chưa check-out
    // của ngày hôm nay là MISSING_CHECKOUT (trừ ca đêm vẫn được phép check-out sang hôm sau).
    @Scheduled(cron = "0 55 23 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void closeMissingCheckouts() {
        LocalDate today = LocalDate.now();
        List<Attendance> open = attendanceRepository.findOpenAttendancesOn(today);
        if (open.isEmpty()) {
            return;
        }

        int closed = 0;
        for (Attendance attendance : open) {
            if (isOvernightShift(attendance)) {
                continue;
            }
            attendance.setStatus(AttendanceStatus.MISSING_CHECKOUT);
            String existingNote = attendance.getNote();
            String autoNote = "Tự động đánh dấu: nhân viên quên check-out / Auto-flagged: employee forgot to check out";
            attendance.setNote(existingNote == null || existingNote.isBlank()
                    ? autoNote
                    : existingNote + " | " + autoNote);
            notifyEmployee(attendance);
            closed++;
        }

        if (closed > 0) {
            attendanceRepository.saveAll(open);
            log.info("Auto-closed {} attendance record(s) without checkout for {}", closed, today);
        }
    }

    private boolean isOvernightShift(Attendance attendance) {
        WorkSchedule schedule = attendance.getSchedule();
        if (schedule == null || schedule.getShift() == null) {
            return false;
        }
        Shift shift = schedule.getShift();
        return shift.getStartTime() != null
                && shift.getEndTime() != null
                && !shift.getEndTime().isAfter(shift.getStartTime());
    }

    private void notifyEmployee(Attendance attendance) {
        if (attendance.getEmployee() == null || attendance.getEmployee().getUser() == null) {
            return;
        }
        Long userId = attendance.getEmployee().getUser().getId();
        notificationService.createForUser(
                userId,
                NotificationType.ATTENDANCE,
                "Quên check-out / Missing checkout",
                "Bạn quên check-out cho ngày " + attendance.getWorkDate()
                        + ". Hãy gửi đơn xin bù check-out để cấp quản lý duyệt."
                        + "\nYou forgot to check out on " + attendance.getWorkDate()
                        + ". Please submit a make-up checkout request for manager approval.",
                "/makeup-checkouts"
        );
    }
}
