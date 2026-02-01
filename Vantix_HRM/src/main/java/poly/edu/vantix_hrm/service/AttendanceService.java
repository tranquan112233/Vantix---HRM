package poly.edu.vantix_hrm.service;

import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.Users;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    Attendance postCheckIn(Integer users);
    Attendance putCheckOut();
    Attendance confirmCheckOut();
    List<Attendance> getEmployeeMonthlyAttendance(Integer users, LocalDate MonthAndYear);
}
