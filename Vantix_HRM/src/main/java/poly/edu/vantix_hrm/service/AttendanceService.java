package poly.edu.vantix_hrm.service;

import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.Employees;
import poly.edu.vantix_hrm.entity.Shifts;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    // Lấy chấm công theo tháng
    List<Attendance> getMonthlyAttendance(Employees employees, LocalDate MonthAndYear);

    // Kiểm tra ID nhân viên
    Employees isEmployeeValid(Integer employeeId);

    // Kiểm tra ca hợp lệ
    Shifts getCurrentShift();

    // Thêm mới chấm công
    Attendance createAttendanceRecord(Employees employees, Shifts shift);
}
