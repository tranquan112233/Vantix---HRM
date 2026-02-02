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
        Users users = userDAO.findById(userID)
                .orElseThrow(() -> new RuntimeException("❌ Không tìm thấy mã nhân viên " + userID + " trên hệ thống."));
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
        Attendance att = new Attendance();
        att.setUser(users);
        att.setShift(shifts);
        att.setWorkDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        att.setCheckIn(LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        att.setCheckOut(null);
        att.setWorkHours(null);
        att.setLateMinutes(lateMinutes);
        att.setEarlyLeaveMinutes(null);
        att.setStatus(Attendance.Status.Draft);
        return attendanceDAO.save(att);
    }

    @Override
    public Attendance putCheckOut() {
        return null;
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
