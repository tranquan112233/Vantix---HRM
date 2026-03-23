package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.DailyWorkSchedules;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.entity.Shift;
import poly.edu.vantix_hrm.repository.AttendanceRepository;
import poly.edu.vantix_hrm.repository.DailyWorkSchedulesRepository;
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
    private final DailyWorkSchedulesRepository dailyWorkSchedulesRepository;

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");


    public List<Attendance> getMonthlyAttendance(Employee employee, LocalDate monthAndYear) {
        return attendanceRepository.getMonthlyAttendance(employee.getEmployeeId(), monthAndYear.getMonthValue(), monthAndYear.getYear());
    }

    public Employee isEmployeeValid(Integer employeeId) {
        return employeeRepository.findById(employeeId).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên (" + employeeId + ") trên hệ thống."));
    }

    // Hàm kiểm tra có lịch làm việc hôm nay không và lấy Shift của ngày đó
    public Shift getScheduledShiftForToday(Employee employee) {
        LocalDate today = LocalDate.now(VIETNAM_ZONE);

        // 1. Tìm xem hôm nay có lịch làm việc không
        String msgSchedule = "Hôm nay bạn không có lịch làm việc";
        DailyWorkSchedules schedule = dailyWorkSchedulesRepository.findByEmployeeIdAndWorkDate(employee.getEmployeeId(), today).orElseThrow(() -> new RuntimeException(msgSchedule));

        // 2. Kiểm tra DayType xem thuộc loại làm việc gì
        if (schedule.getDayType() != DailyWorkSchedules.DayType.WORK) {
            throw new RuntimeException("Hôm nay là ngày " + schedule.getDayType() + ", bạn không cần chấm công.");
        }

        // 3. Kiểm tra lịch này đã gán Ca (Shift) chưa
        if (schedule.getShift() == null) {
            throw new RuntimeException("Lịch làm việc hôm nay của bạn chưa được gán ca cụ thể.");
        }
        return schedule.getShift();
    }

    // Hàm kiểm tra thời gian checkIn
    public void validateCheckInTime(Shift shift) {
        LocalTime now = LocalTime.now(VIETNAM_ZONE);
        LocalTime startAllow = shift.getStartTime().minusMinutes(15); // Cho phép CheckIn sớm 15p
        LocalTime endAllow = shift.getEndTime();
        if (now.isBefore(startAllow)) {
            throw new RuntimeException("Chưa đến giờ chấm công. Ca làm việc bắt đầu lúc " + shift.getStartTime() + ", bạn chỉ được Check-in sớm tối đa 15 phút.");
        }
        if (now.isAfter(endAllow)) {
            throw new RuntimeException("Đã kết thúc ca làm việc, không thể Check-in.");
        }
    }

    // Hàm lưu thông tin check-in lên
    public Attendance createAttendanceRecord(Employee employee, Shift shift) {

        LocalDate today = LocalDate.now(VIETNAM_ZONE);
        LocalTime now = LocalTime.now(VIETNAM_ZONE);

        // 1. Kiểm tra xem hôm nay nhân viên đã chấm công cho ca này chưa
        boolean alreadyChecked = attendanceRepository.isAlreadyCheckedIn(employee.getEmployeeId(), shift.getShiftId(), today);
        if (alreadyChecked) {
            throw new RuntimeException("Đã chấm công cho ca " + shift.getShiftName() + " hôm nay rồi.");
        }

        // 2. Tính toán số phút đi trễ (nếu có)
        int lateMinutes = 0;
        if (now.isAfter(shift.getStartTime())) {
            lateMinutes = (int) Duration.between(shift.getStartTime(), now).toMinutes();
        }

        // 3. Khởi tạo đối tượng Attendance và lưu vào DB với trạng thái DRAFT
        Attendance att = Attendance.builder().employee(employee).shift(shift).workDate(today).checkIn(now).lateMinutes(lateMinutes).status(Attendance.AttendanceStatus.DRAFT).build();

        return attendanceRepository.save(att);
    }


//    /* ================= SHIFT ================= */
//
//    public Shift getCurrentShift() {
//
//        LocalTime now = LocalTime.now(VIETNAM_ZONE);
//
//        return shiftsRepository.findAll().stream().filter(shift -> {
//            LocalTime start = shift.getStartTime().minusMinutes(15);
//            LocalTime end = shift.getEndTime();
//            return !now.isBefore(start) && !now.isAfter(end);
//        }).findFirst().orElseThrow(() -> new RuntimeException("Hiện tại không nằm trong khung giờ chấm công hợp lệ."));
//    }
//
//    /* ================= CHECK IN ================= */
//
//    public Attendance createAttendanceRecord(Employee employee, Shift shift) {
//
//        LocalDate today = LocalDate.now(VIETNAM_ZONE);
//        LocalTime now = LocalTime.now(VIETNAM_ZONE);
//
//        boolean alreadyChecked = attendanceRepository.isAlreadyCheckedIn(employee.getEmployeeId(), shift.getShiftId(), today);
//
//        if (alreadyChecked) {
//            throw new RuntimeException("Đã chấm công cho ca " + shift.getShiftName() + " hôm nay rồi.");
//        }
//
//        int lateMinutes = 0;
//        if (now.isAfter(shift.getStartTime())) {
//            lateMinutes = (int) Duration.between(shift.getStartTime(), now).toMinutes();
//        }
//
//        Attendance att = Attendance.builder().employee(employee).shift(shift).workDate(today).checkIn(now).lateMinutes(lateMinutes).status(Attendance.AttendanceStatus.DRAFT).build();
//
//        return attendanceRepository.save(att);
//    }
//
//    /* ================= CHECK OUT ================= */
//
//    public Attendance updateAttendanceRecord(Attendance att, boolean isAuto) {
//
//        Shift shift = att.getShift();
//        LocalTime now = LocalTime.now(VIETNAM_ZONE);
//
//        if (isAuto) {
//            att.setCheckOut(shift.getEndTime());
//            att.setEarlyLeaveMinutes(0);
//            att.setStatus(Attendance.AttendanceStatus.PENDING);
//        } else {
//            att.setCheckOut(now);
//
//            long diff = Duration.between(now, shift.getEndTime()).toMinutes();
//            att.setEarlyLeaveMinutes((int) Math.max(0, diff));
//            att.setStatus(Attendance.AttendanceStatus.APPROVED);
//        }
//
//        return attendanceRepository.save(att);
//    }
//
//    /* ================= FIND RECORD ================= */
//
//    public Attendance findAttendanceToUpdate(Employee employee, Shift shift) {
//
//        LocalDate today = LocalDate.now(VIETNAM_ZONE);
//
//        Attendance att = attendanceRepository.findExistingAttendance(employee.getEmployeeId(), shift.getShiftId(), today).orElseThrow(() -> new RuntimeException("Bạn chưa chấm công ca " + shift.getShiftName() + " ngày " + today));
//
//        switch (att.getStatus()) {
//            case APPROVED, REJECTED ->
//                    throw new RuntimeException("Ca này đã có trạng thái " + att.getStatus() + ", không thể chỉnh sửa.");
//
//            case PENDING -> throw new RuntimeException("Bạn đã được Check Out, vui lòng xác nhận.");
//
//            default -> {
//                return att;
//            }
//        }
//    }
//
//    public Attendance findPendingAutoCheckOut(Employee employee) {
//        LocalDate today = LocalDate.now(VIETNAM_ZONE);
//
//        return attendanceRepository.findPendingAttendance(employee.getEmployeeId(), today).orElseThrow(() -> new RuntimeException("Bạn không có yêu cầu xác nhận công nào đang chờ xử lý."));
//    }
//
//    public Attendance finalizeAndApproveCheckOut(Attendance att) {
//        att.setStatus(Attendance.AttendanceStatus.APPROVED);
//        return attendanceRepository.save(att);
//    }
}