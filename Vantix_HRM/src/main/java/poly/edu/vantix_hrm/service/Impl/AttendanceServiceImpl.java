package poly.edu.vantix_hrm.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.dao.AttendanceDAO;
import poly.edu.vantix_hrm.dao.ShiftsDAO;
import poly.edu.vantix_hrm.dao.UserDAO;
import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.Users;
import poly.edu.vantix_hrm.service.AttendanceService;

import java.time.LocalDate;
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
    public Attendance postCheckIn(Integer users) {
        return null;
    }

    @Override
    public Attendance putCheckOut(){
        return null;
    }

    @Override
    public Attendance confirmCheckOut(){
        return null;
    }

    @Override
    public List<Attendance> getEmployeeMonthlyAttendance(Integer UserID, LocalDate MonthAndYear) {
        int month = MonthAndYear.getMonthValue();
        int year = MonthAndYear.getYear();
        return attendanceDAO.findAttendanceByMonth(UserID, month, year);
    }
}
