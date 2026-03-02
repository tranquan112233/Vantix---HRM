package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.entity.Shift;
import poly.edu.vantix_hrm.repository.AttendanceRepository;
import poly.edu.vantix_hrm.repository.EmployeeRepository;
import poly.edu.vantix_hrm.repository.ShiftsRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftsRepository shiftsRepository;

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /* ================= MONTHLY ================= */

    public List<Attendance> getMonthlyAttendance(Employee employee, LocalDate monthAndYear) {
        return attendanceRepository.getMonthlyAttendance(
                employee.getEmployeeId(),
                monthAndYear.getMonthValue(),
                monthAndYear.getYear()
        );
    }

    /* ================= VALIDATE EMPLOYEE ================= */

    public Employee isEmployeeValid(Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy nhân viên (" + employeeId + ") trên hệ thống."));
    }

    /* ================= SHIFT ================= */

    public Shift getCurrentShift() {

        LocalTime now = LocalTime.now(VIETNAM_ZONE);

        return shiftsRepository.findAll().stream()
                .filter(shift -> {
                    LocalTime start = shift.getStartTime().minusMinutes(15);
                    LocalTime end = shift.getEndTime();
                    return !now.isBefore(start) && !now.isAfter(end);
                })
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Hiện tại không nằm trong khung giờ chấm công hợp lệ."));
    }

    /* ================= CHECK IN ================= */

    public Attendance createAttendanceRecord(Employee employee, Shift shift) {

        LocalDate today = LocalDate.now(VIETNAM_ZONE);
        LocalTime now = LocalTime.now(VIETNAM_ZONE);

        boolean alreadyChecked =
                attendanceRepository.isAlreadyCheckedIn(
                        employee.getEmployeeId(),
                        shift.getShiftId(),
                        today
                );

        if (alreadyChecked) {
            throw new RuntimeException(
                    "Đã chấm công cho ca " + shift.getShiftName() + " hôm nay rồi.");
        }

        int lateMinutes = 0;
        if (now.isAfter(shift.getStartTime())) {
            lateMinutes = (int) Duration
                    .between(shift.getStartTime(), now)
                    .toMinutes();
        }

        Attendance att = Attendance.builder()
                .employee(employee)
                .shift(shift)
                .workDate(today)
                .checkIn(now)
                .lateMinutes(lateMinutes)
                .status(Attendance.AttendanceStatus.DRAFT)
                .build();

        return attendanceRepository.save(att);
    }

    /* ================= CHECK OUT ================= */

    public Attendance updateAttendanceRecord(Attendance att, boolean isAuto) {

        Shift shift = att.getShift();
        LocalTime now = LocalTime.now(VIETNAM_ZONE);

        if (isAuto) {
            att.setCheckOut(shift.getEndTime());
            att.setEarlyLeaveMinutes(0);
            att.setStatus(Attendance.AttendanceStatus.PENDING);
        } else {
            att.setCheckOut(now);

            long diff = Duration.between(now, shift.getEndTime()).toMinutes();
            att.setEarlyLeaveMinutes((int) Math.max(0, diff));
            att.setStatus(Attendance.AttendanceStatus.APPROVED);
        }

        return attendanceRepository.save(att);
    }

    /* ================= FIND RECORD ================= */

    public Attendance findAttendanceToUpdate(Employee employee, Shift shift) {

        LocalDate today = LocalDate.now(VIETNAM_ZONE);

        Attendance att = attendanceRepository
                .findExistingAttendance(employee.getEmployeeId(),
                        shift.getShiftId(),
                        today)
                .orElseThrow(() ->
                        new RuntimeException("Bạn chưa chấm công ca "
                                + shift.getShiftName()
                                + " ngày " + today));

        switch (att.getStatus()) {
            case APPROVED, REJECTED ->
                    throw new RuntimeException(
                            "Ca này đã có trạng thái "
                                    + att.getStatus()
                                    + ", không thể chỉnh sửa.");

            case PENDING ->
                    throw new RuntimeException(
                            "Bạn đã được Check Out, vui lòng xác nhận.");

            default -> {
                return att;
            }
        }
    }

    public Attendance findPendingAutoCheckOut(Employee employee) {
        LocalDate today = LocalDate.now(VIETNAM_ZONE);

        return attendanceRepository
                .findPendingAttendance(employee.getEmployeeId(), today)
                .orElseThrow(() ->
                        new RuntimeException("Bạn không có yêu cầu xác nhận công nào đang chờ xử lý."));
    }

    public Attendance finalizeAndApproveCheckOut(Attendance att) {
        att.setStatus(Attendance.AttendanceStatus.APPROVED);
        return attendanceRepository.save(att);
    }
}