package poly.edu.vantix_hrm.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.repository.AttendanceRepository;
import poly.edu.vantix_hrm.repository.EmployeeRepository;
import poly.edu.vantix_hrm.repository.ShiftRepository;
import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.entity.Shift;
import poly.edu.vantix_hrm.service.AttendanceService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ShiftRepository shiftsShiftRepository;

    @Override
    public List<Attendance> getMonthlyAttendance(Employee employees, LocalDate MonthAndYear) {
        int Month = MonthAndYear.getMonthValue();
        int Year = MonthAndYear.getYear();
        return attendanceRepository.getMonthlyAttendance(employees.getEmployeeId(), Month, Year);
    }

    @Override
    public Employee isEmployeeValid(Integer employeeId) {
        String msgError = "Không tìm thấy nhân viên (" + employeeId + ") trên hệ thống.";
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new RuntimeException(msgError));
        return employee;
    }

    @Override
    public Shift getCurrentShift() {
        String msgError = "Hiện tại không nằm trong khung giờ chấm công hợp lệ.";
        List<Shift> getAllShifts = shiftsShiftRepository.findAll();
        LocalTime VietNam = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        for (Shift shift : getAllShifts) {
            LocalTime startShift = shift.getStartTime().minusMinutes(15);
            LocalTime endShift = shift.getEndTime();
            if (!VietNam.isBefore(startShift) && !VietNam.isAfter(endShift)) return shift;
        }
        throw new RuntimeException(msgError);
    }

    @Override
    public Attendance createAttendanceRecord(Employee employees, Shift shift) {
        String msgError = "Đã chấm công cho ca " + shift.getShiftName() + " hôm nay rồi.";
        LocalDate vietNam = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalTime gioVietNam = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Boolean isAlreadyCheckedIn = attendanceRepository.isAlreadyCheckedIn(employees.getEmployeeId(), shift.getShiftId(), vietNam);
        if (isAlreadyCheckedIn) {
            throw new RuntimeException(msgError);
        }
        Integer lateMinutes = 0;
        if (gioVietNam.isAfter(shift.getStartTime())) {
            lateMinutes = (int) Duration.between(shift.getStartTime(), gioVietNam).toMinutes();
        }
        Attendance att = new Attendance();
        att.setEmployee(employees);
        att.setShift(shift);
        att.setWorkDate(vietNam);
        att.setCheckIn(LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        att.setCheckOut(null);
        att.setLateMinutes(lateMinutes);
        att.setEarlyLeaveMinutes(null);
        att.setStatus(Attendance.AttendanceStatus.DRAFT);
        return attendanceRepository.save(att);
    }

    @Override
    public Attendance updateAttendanceRecord(Attendance att, Boolean isAuto) {
        if (isAuto) {
            att.setCheckOut(att.getShift().getEndTime());
            att.setEarlyLeaveMinutes(0);
            att.setStatus(Attendance.AttendanceStatus.PENDING);
            return attendanceRepository.save(att);
        }
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        att.setCheckOut(now);
        Shift shift = att.getShift();
        Long minutesDiff = Duration.between(now, shift.getEndTime()).toMinutes();
        att.setEarlyLeaveMinutes((int) Math.max(0, minutesDiff));
        att.setStatus(Attendance.AttendanceStatus.APPROVED);
        return attendanceRepository.save(att);
    }

    @Override
    public Attendance findAttendanceToUpdate(Employee employee, Shift shift) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Attendance att = attendanceRepository.findExistingAttendance(employee.getEmployeeId(), shift.getShiftId(), today).orElseThrow(() -> {
            String notFoundMsg = "Bạn chưa chấm công ca " + shift.getShiftName() + " ngày " + today;
            return new RuntimeException(notFoundMsg);
        });
        if (att.getStatus() == Attendance.AttendanceStatus.APPROVED || att.getStatus() == Attendance.AttendanceStatus.REJECTED) {
            String statusErrorMsg = "Ca này đã có trạng thái " + att.getStatus() + ", không thể chỉnh sửa.";
            throw new RuntimeException(statusErrorMsg);
        }
        if (att.getStatus() == Attendance.AttendanceStatus.PENDING) {
            String statusErrorMsg = "Bạn đã được Check Out xin vui lòng xác nhận.";
            throw new RuntimeException(statusErrorMsg);
        }
        return att;
    }

    @Override
    public Attendance findPendingAutoCheckOut(Employee employee) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        return attendanceRepository.findPendingAttendance(employee.getEmployeeId(), today)
                .orElseThrow(() -> new RuntimeException("Bạn không có yêu cầu xác nhận công nào đang chờ xử lý."));
    }

    @Override
    public Attendance finalizeAndApproveCheckOut(Attendance att) {
        att.setStatus(Attendance.AttendanceStatus.APPROVED);
        return attendanceRepository.save(att);
    }
}
