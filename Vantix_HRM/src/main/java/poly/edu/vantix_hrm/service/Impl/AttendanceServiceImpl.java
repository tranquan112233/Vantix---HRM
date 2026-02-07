package poly.edu.vantix_hrm.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.dao.AttendanceDAO;
import poly.edu.vantix_hrm.dao.EmployeesDAO;
import poly.edu.vantix_hrm.dao.ShiftsDAO;
import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.Employees;
import poly.edu.vantix_hrm.entity.Shifts;
import poly.edu.vantix_hrm.service.AttendanceService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceDAO attendanceDAO;

    @Autowired
    private EmployeesDAO employeesDAO;

    @Autowired
    private ShiftsDAO shiftsDAO;

    @Override
    public List<Attendance> getMonthlyAttendance(Employees employees, LocalDate MonthAndYear) {
        int Month = MonthAndYear.getMonthValue();
        int Year = MonthAndYear.getYear();
        return attendanceDAO.getMonthlyAttendance(employees.getEmployeeId(), Month, Year);
    }

    @Override
    public Employees isEmployeeValid(Integer employeeId) {
        String msgError = "Không tìm thấy nhân viên (" + employeeId + ") trên hệ thống.";
        Employees employee = employeesDAO.findById(employeeId).orElseThrow(() -> new RuntimeException(msgError));
        return employee;
    }

    @Override
    public Shifts getCurrentShift() {
        String msgError = "Hiện tại không nằm trong khung giờ chấm công hợp lệ.";
        List<Shifts> getAllShifts = shiftsDAO.findAll();
        LocalTime VietNam = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        for (Shifts shift : getAllShifts) {
            LocalTime startShift = shift.getStartTime().minusMinutes(15);
            LocalTime endShift = shift.getEndTime();
            if (!VietNam.isBefore(startShift) && !VietNam.isAfter(endShift)) return shift;
        }
        throw new RuntimeException(msgError);
    }

    @Override
    public Attendance createAttendanceRecord(Employees employees, Shifts shift) {
        String msgError = "Đã chấm công cho ca " + shift.getShiftName() + " hôm nay rồi.";
        LocalDate vietNam = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalTime gioVietNam = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Boolean isAlreadyCheckedIn = attendanceDAO.isAlreadyCheckedIn(employees.getEmployeeId(), shift.getShiftId(), vietNam);
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
        return attendanceDAO.save(att);
    }

    @Override
    public Attendance updateAttendanceRecord(Attendance att, Boolean isAuto) {
        if (isAuto) {
            att.setCheckOut(att.getShift().getEndTime());
            att.setEarlyLeaveMinutes(0);
            att.setStatus(Attendance.AttendanceStatus.PENDING);
            return attendanceDAO.save(att);
        }
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        att.setCheckOut(now);
        Shifts shift = att.getShift();
        Long minutesDiff = Duration.between(now, shift.getEndTime()).toMinutes();
        att.setEarlyLeaveMinutes((int) Math.max(0, minutesDiff));
        att.setStatus(Attendance.AttendanceStatus.APPROVED);
        return attendanceDAO.save(att);
    }

    @Override
    public Attendance findAttendanceToUpdate(Employees employee, Shifts shift) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Attendance att = attendanceDAO.findExistingAttendance(employee.getEmployeeId(), shift.getShiftId(), today).orElseThrow(() -> {
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
    public Attendance findPendingAutoCheckOut(Employees employee) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        return attendanceDAO.findPendingAttendance(employee.getEmployeeId(), today)
                .orElseThrow(() -> new RuntimeException("Bạn không có yêu cầu xác nhận công nào đang chờ xử lý."));
    }

    @Override
    public Attendance finalizeAndApproveCheckOut(Attendance att) {
        att.setStatus(Attendance.AttendanceStatus.APPROVED);
        return attendanceDAO.save(att);
    }
}
