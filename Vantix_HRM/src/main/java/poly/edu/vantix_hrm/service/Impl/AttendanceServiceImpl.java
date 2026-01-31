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

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private ShiftsDAO shiftsDAO;

    @Autowired
    private AttendanceDAO attendanceDAO;

    @Override
    public Attendance checkIn(Integer UserID) {
        Users user = userDAO.findById(UserID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên có ID: " + UserID));

        Shifts shifts = shiftsDAO.findById(1)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca có ID: " + 1));

        Attendance att = new Attendance();
        att.setUser(user);
        att.setShift(shifts);
        att.setWorkDate(LocalDate.now());
        att.setCheckIn(LocalTime.now());
        return attendanceDAO.save(att);
    }
}
