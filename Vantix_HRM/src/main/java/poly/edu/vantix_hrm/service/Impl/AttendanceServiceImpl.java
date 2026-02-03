package poly.edu.vantix_hrm.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.dao.AttendanceDAO;
import poly.edu.vantix_hrm.dao.ShiftsDAO;
import poly.edu.vantix_hrm.dao.UserDAO;
import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.Shifts;
import poly.edu.vantix_hrm.entity.Users;
import poly.edu.vantix_hrm.service.AttendanceService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private ShiftsDAO shiftsDAO;

    @Autowired
    private AttendanceDAO attendanceDAO;

    @Override
    public Users checkUser(Integer userID) {
        Users users = userDAO.findById(userID).orElseThrow(() -> new RuntimeException("❌ Không tìm thấy mã nhân viên " + userID + " trên hệ thống."));
        return users;
    }

    @Override
    public Shifts checkShift() {
        List<Shifts> allShift = shiftsDAO.findAll();
        LocalTime gioVN = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        for (Shifts shifts : allShift) {
            LocalTime startTime = shifts.getStartTime().minusMinutes(15);
            LocalTime endTime = shifts.getEndTime();
            if (!gioVN.isBefore(startTime) && !gioVN.isAfter(endTime)) {
                return shifts;
            }
        }
        throw new RuntimeException("⚠️ Hiện tại không nằm trong khung giờ chấm công hợp lệ!");
    }

    @Override
    public Attendance postCheckIn(Shifts shifts, Users users, Integer lateMinutes) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        boolean isExist = attendanceDAO.existsByUser_UserIDAndShift_ShiftIDAndWorkDate(users.getUserID(), shifts.getShiftID(), today);
        if (isExist) {
            throw new RuntimeException("⛔ Bạn đã chấm công cho ca " + shifts.getShiftName() + " hôm nay rồi!");
        }
        Attendance att = new Attendance();
        att.setUser(users);
        att.setShift(shifts);
        att.setWorkDate(today);
        att.setCheckIn(LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        att.setCheckOut(null);
        att.setWorkHours(null);
        att.setLateMinutes(lateMinutes);
        att.setEarlyLeaveMinutes(null);
        att.setStatus(Attendance.Status.Draft);
        return attendanceDAO.save(att);
    }

    // Xin về sớm
    @Override
    public Attendance putCheckOut(Integer userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Attendance att = attendanceDAO.findByUser_UserIDAndWorkDateAndCheckOutIsNull(userId, today).orElseThrow(() -> new RuntimeException("⚠️ Bạn chưa Check-in hoặc đã Check-out rồi!"));
        att.setCheckOut(now);
        // 4. LOGIC TÍNH SỚM/TRỄ
        Shifts shift = att.getShift();
        long earlyMinutes = 0;
        // Nếu về trước giờ kết thúc ca (Ví dụ: Ca 17:00, về lúc 16:30 -> Sớm 30p)
        if (now.isBefore(shift.getEndTime())) {
            earlyMinutes = java.time.Duration.between(now, shift.getEndTime()).toMinutes();
        }
        att.setEarlyLeaveMinutes((int) earlyMinutes);
        // 5. LOGIC TÍNH GIỜ LÀM (WorkHours)
        long minutesWorked = Duration.between(att.getCheckIn(), now).toMinutes();
        BigDecimal bdMinutes = BigDecimal.valueOf(minutesWorked);
        att.setWorkHours(bdMinutes.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
        // 6. Cập nhật trạng thái
        att.setStatus(Attendance.Status.Approved);
        // 7. Lưu xuống DB
        return attendanceDAO.save(att);
    }

    @Override
    public Attendance confirmCheckOut() {
        return null;
    }

    @Override
    public List<Attendance> getEmployeeMonthlyAttendance(Integer UserID, LocalDate MonthAndYear) {
        int month = MonthAndYear.getMonthValue();
        int year = MonthAndYear.getYear();
        return attendanceDAO.findAttendanceByMonth(UserID, month, year);
    }
}
