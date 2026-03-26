package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.attendancemanagement.RejectedAttendanceDTO;
import poly.edu.vantix_hrm.entity.Attendance;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.repository.AttendanceRepository;
import poly.edu.vantix_hrm.repository.EmployeeRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceManagementService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public List<RejectedAttendanceDTO> getRejectedAttendancesForManager(Integer managerId) {
        // 1. Lấy thông tin phòng ban
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin Trưởng phòng"));
        Integer departmentId = manager.getDepartment().getDepartmentId();

        // 2. Lấy TOÀN BỘ nhân viên trong phòng ban đó
        List<Employee> allEmployees = employeeRepository.findByDepartment_DepartmentId(departmentId);

        // --- CODE MỚI THÊM: Loại bỏ Trưởng phòng khỏi danh sách hiển thị ---
        allEmployees.removeIf(emp -> emp.getEmployeeId().equals(managerId));

        // 3. Lấy các phiếu REJECTED trong 3 ngày qua
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(2);
        List<Attendance> attendances = attendanceRepository
                .findRejectedByDepartmentAndDateRange(departmentId, startDate, endDate);

        // Group phiếu theo Mã Nhân Viên để dễ tìm kiếm
        Map<Integer, List<Attendance>> attendanceMap = attendances.stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getEmployeeId()));

        List<RejectedAttendanceDTO> result = new ArrayList<>();

        // 4. Lặp qua tất cả nhân viên (đã loại trừ Trưởng phòng)
        for (Employee emp : allEmployees) {
            List<Attendance> empAttendances = attendanceMap.getOrDefault(emp.getEmployeeId(), Collections.emptyList());

            if (empAttendances.isEmpty()) {
                // Nếu không có phiếu nào -> Trả về DTO chỉ có thông tin nhân viên, còn lại null
                result.add(RejectedAttendanceDTO.builder()
                        .employeeId(emp.getEmployeeId())
                        .fullName(emp.getFullName())
                        .build());
            } else {
                // Nếu có phiếu -> Map từng phiếu thành DTO
                for (Attendance a : empAttendances) {
                    result.add(RejectedAttendanceDTO.builder()
                            .attendanceId(a.getAttendanceId())
                            .employeeId(emp.getEmployeeId())
                            .fullName(emp.getFullName())
                            .workDate(a.getWorkDate())
                            .shiftId(a.getShift().getShiftId())
                            .shiftName(a.getShift().getShiftName())
                            .checkIn(a.getCheckIn())
                            .checkOut(a.getCheckOut())
                            .lateMinutes(a.getLateMinutes())
                            .earlyLeaveMinutes(a.getEarlyLeaveMinutes())
                            .status(a.getStatus().name())
                            .build());
                }
            }
        }

        return result;
    }

    public void approveAttendance(Integer attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi chấm công"));
        attendance.setStatus(Attendance.AttendanceStatus.APPROVED);
        attendanceRepository.save(attendance);
    }
}