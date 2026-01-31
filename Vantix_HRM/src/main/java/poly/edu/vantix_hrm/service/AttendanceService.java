package poly.edu.vantix_hrm.service;

import poly.edu.vantix_hrm.entity.Attendance;

public interface AttendanceService {
    Attendance checkIn(Integer UserID);
}
