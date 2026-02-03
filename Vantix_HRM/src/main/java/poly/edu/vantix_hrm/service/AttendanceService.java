package poly.edu.vantix_hrm.service;

import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.Shifts;
import poly.edu.vantix_hrm.entity.Users;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    Users checkUser(Integer userID);
    Shifts checkShift();
    Attendance postCheckIn(Shifts shifts, Users users, Integer lateMinutes);
    Attendance putCheckOut(Integer userId);
    Attendance confirmCheckOut();
    List<Attendance> getEmployeeMonthlyAttendance(Integer users, LocalDate MonthAndYear);
}
