package poly.edu.vantix_hrm.service;

import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.entity.Shift;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    // Lấy chấm công theo tháng
    List<Attendance> getMonthlyAttendance(Employee employees, LocalDate MonthAndYear);

    // Kiểm tra ID nhân viên
    Employee isEmployeeValid(Integer employeeId);

    // Kiểm tra ca hợp lệ
    Shift getCurrentShift();

    // Hàm CheckIn
    Attendance createAttendanceRecord(Employee employee, Shift shift);

    // Xác định ca cần CheckOut
    Attendance findAttendanceToUpdate(Employee employee, Shift shift);

    // Hàm CheckOut
    Attendance updateAttendanceRecord(Attendance att, Boolean isAuto);

    // Xác nhận ca đã được CheckOutAuto
    Attendance findPendingAutoCheckOut(Employee employee);

    // Hàm xác nhận CheckOut
    Attendance finalizeAndApproveCheckOut(Attendance att);
}
