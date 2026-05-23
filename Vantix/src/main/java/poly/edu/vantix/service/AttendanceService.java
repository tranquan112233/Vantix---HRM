package poly.edu.vantix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.AttendanceCheckRequest;
import poly.edu.vantix.dto.request.AttendanceGenerateRequest;
import poly.edu.vantix.dto.response.AttendanceGenerateResponse;
import poly.edu.vantix.dto.response.AttendanceResponse;
import poly.edu.vantix.entity.Attendance;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.WorkLocation;
import poly.edu.vantix.entity.WorkSchedule;
import poly.edu.vantix.entity.enums.AttendanceStatus;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.repository.AttendanceRepository;
import poly.edu.vantix.repository.EmployeeRepository;
import poly.edu.vantix.repository.WorkScheduleRepository;
import poly.edu.vantix.util.GeoUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final WorkScheduleRepository scheduleRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            WorkScheduleRepository scheduleRepository,
            EmployeeRepository employeeRepository
    ) {
        this.attendanceRepository = attendanceRepository;
        this.scheduleRepository = scheduleRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> search(LocalDate from, LocalDate to, Long employeeId) {
        return attendanceRepository.search(from, to, employeeId).stream()
                .map(AttendanceResponse::fromEntity)
                .toList();
    }

    @Transactional
    public AttendanceResponse checkIn(Long userId, AttendanceCheckRequest request) {
        Employee employee = resolveEmployee(userId);
        LocalDate today = LocalDate.now();

        WorkSchedule schedule = scheduleRepository.findByEmployeeAndDate(employee.getId(), today)
                .orElseThrow(() -> new BusinessException("No work schedule for today"));

        validateLocation(schedule, request);

        Attendance attendance = attendanceRepository
                .findByEmployeeAndDate(employee.getId(), today)
                .orElseGet(() -> {
                    Attendance fresh = new Attendance();
                    fresh.setEmployee(employee);
                    fresh.setSchedule(schedule);
                    fresh.setWorkDate(today);
                    return fresh;
                });

        if (attendance.getCheckInAt() != null) {
            throw new BusinessException("Already checked in for today");
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setSchedule(schedule);
        attendance.setCheckInAt(now);
        attendance.setCheckInLat(request.getLatitude());
        attendance.setCheckInLng(request.getLongitude());
        attendance.setCheckInDistance(distanceFromSchedule(schedule, request));
        attendance.setStatus(computeInitialStatus(now.toLocalTime(), schedule.getShift().getStartTime()));

        return AttendanceResponse.fromEntity(attendanceRepository.save(attendance));
    }

    @Transactional
    public AttendanceResponse checkOut(Long userId, AttendanceCheckRequest request) {
        Employee employee = resolveEmployee(userId);
        LocalDate today = LocalDate.now();

        Attendance attendance = findOpenAttendanceForCheckout(employee.getId(), today)
                .orElseThrow(() -> new BusinessException("You have not checked in for the current shift"));

        if (attendance.getCheckInAt() == null) {
            throw new BusinessException("You have not checked in today");
        }
        if (attendance.getCheckOutAt() != null) {
            throw new BusinessException("Already checked out for today");
        }

        WorkSchedule schedule = attendance.getSchedule();
        if (schedule != null) {
            validateLocation(schedule, request);
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOutAt(now);
        attendance.setCheckOutLat(request.getLatitude());
        attendance.setCheckOutLng(request.getLongitude());
        attendance.setCheckOutDistance(distanceFromSchedule(schedule, request));
        attendance.setStatus(computeFinalStatus(attendance, schedule));

        return AttendanceResponse.fromEntity(attendanceRepository.save(attendance));
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getTodayForUser(Long userId) {
        Employee employee = resolveEmployee(userId);
        LocalDate today = LocalDate.now();
        return findOpenAttendanceForCheckout(employee.getId(), today)
                .or(() -> attendanceRepository.findByEmployeeAndDate(employee.getId(), today))
                .map(AttendanceResponse::fromEntity)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Long resolveEmployeeIdByUserId(Long userId) {
        return resolveEmployee(userId).getId();
    }

    @Transactional
    public AttendanceGenerateResponse generateFromSchedules(Employee actor, AttendanceGenerateRequest request) {
        if (request.getFromDate() != null
                && request.getToDate() != null
                && request.getFromDate().isAfter(request.getToDate())) {
            throw new BusinessException("toDate", "End date must be on or after start date");
        }

        Long scopedDepartmentId = resolveScopedDepartmentId(actor, request.getDepartmentId());
        Long scopedEmployeeId = request.getEmployeeId();
        if (request.getEmployeeId() != null) {
            Employee target = employeeRepository.findActiveById(request.getEmployeeId())
                    .orElseThrow(() -> new BusinessException("employeeId", "Employee does not exist"));
            ensureAttendanceScope(actor, target);
            scopedEmployeeId = target.getId();
        }

        List<WorkSchedule> schedules = scheduleRepository.search(
                request.getFromDate(),
                request.getToDate(),
                scopedEmployeeId,
                scopedDepartmentId
        );

        int created = 0;
        int skipped = 0;
        Set<Long> employeeIds = new HashSet<>();
        List<Attendance> toSave = new ArrayList<>();

        for (WorkSchedule schedule : schedules) {
            if (schedule.getEmployee() == null || schedule.getShift() == null) {
                skipped++;
                continue;
            }

            employeeIds.add(schedule.getEmployee().getId());
            boolean exists = attendanceRepository
                    .findByEmployeeAndDate(schedule.getEmployee().getId(), schedule.getWorkDate())
                    .isPresent();
            if (exists) {
                skipped++;
                continue;
            }

            Attendance attendance = new Attendance();
            attendance.setEmployee(schedule.getEmployee());
            attendance.setSchedule(schedule);
            attendance.setWorkDate(schedule.getWorkDate());
            attendance.setCheckInAt(LocalDateTime.of(schedule.getWorkDate(), schedule.getShift().getStartTime()));

            LocalDateTime checkOutAt = LocalDateTime.of(schedule.getWorkDate(), schedule.getShift().getEndTime());
            if (!schedule.getShift().getEndTime().isAfter(schedule.getShift().getStartTime())) {
                checkOutAt = checkOutAt.plusDays(1);
            }
            attendance.setCheckOutAt(checkOutAt);

            WorkLocation location = schedule.getLocation();
            if (location != null) {
                attendance.setCheckInLat(location.getLatitude());
                attendance.setCheckInLng(location.getLongitude());
                attendance.setCheckInDistance(0d);
                attendance.setCheckOutLat(location.getLatitude());
                attendance.setCheckOutLng(location.getLongitude());
                attendance.setCheckOutDistance(0d);
            }
            attendance.setStatus(AttendanceStatus.ON_TIME);
            attendance.setNote("Auto-generated from work schedule");
            toSave.add(attendance);
            created++;
        }

        if (!toSave.isEmpty()) {
            attendanceRepository.saveAll(toSave);
        }

        return AttendanceGenerateResponse.builder()
                .created(created)
                .skipped(skipped)
                .employeesProcessed(employeeIds.size())
                .schedulesProcessed(schedules.size())
                .build();
    }

    private Employee resolveEmployee(Long userId) {
        return employeeRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new BusinessException("No employee profile linked to your account"));
    }

    private Long resolveScopedDepartmentId(Employee actor, Long requestedDepartmentId) {
        if (isAdmin(actor)) {
            return requestedDepartmentId;
        }
        Long actorDepartmentId = actor != null && actor.getDepartment() != null ? actor.getDepartment().getId() : null;
        if (actorDepartmentId == null) {
            throw new BusinessException("Current employee is not assigned to a department");
        }
        if (requestedDepartmentId != null && !actorDepartmentId.equals(requestedDepartmentId)) {
            throw new BusinessException("departmentId", "You can only generate attendance for employees in your department");
        }
        return actorDepartmentId;
    }

    private void ensureAttendanceScope(Employee actor, Employee target) {
        if (isAdmin(actor)) {
            return;
        }
        Long actorDepartmentId = actor != null && actor.getDepartment() != null ? actor.getDepartment().getId() : null;
        if (actorDepartmentId == null) {
            throw new BusinessException("Current employee is not assigned to a department");
        }
        Long targetDepartmentId = target != null && target.getDepartment() != null ? target.getDepartment().getId() : null;
        if (!actorDepartmentId.equals(targetDepartmentId)) {
            throw new BusinessException("employeeId", "You can only generate attendance for employees in your department");
        }
    }

    private boolean isAdmin(Employee actor) {
        return actor != null
                && actor.getUser() != null
                && actor.getUser().getRole() != null
                && "Admin".equalsIgnoreCase(actor.getUser().getRole().getName());
    }

    private void validateLocation(WorkSchedule schedule, AttendanceCheckRequest request) {
        WorkLocation location = schedule.getLocation();
        if (location == null) {
            return;
        }
        double distance = GeoUtils.distanceInMeters(
                location.getLatitude(), location.getLongitude(),
                request.getLatitude(), request.getLongitude()
        );
        if (distance > location.getRadiusMeters()) {
            throw new BusinessException(String.format(
                    "You are %d meters away from %s (allowed %d m)",
                    Math.round(distance),
                    location.getName(),
                    location.getRadiusMeters()
            ));
        }
    }

    private Double distanceFromSchedule(WorkSchedule schedule, AttendanceCheckRequest request) {
        if (schedule == null || schedule.getLocation() == null) {
            return null;
        }
        WorkLocation location = schedule.getLocation();
        return GeoUtils.distanceInMeters(
                location.getLatitude(), location.getLongitude(),
                request.getLatitude(), request.getLongitude()
        );
    }

    private AttendanceStatus computeInitialStatus(LocalTime checkInTime, LocalTime shiftStart) {
        return checkInTime.isAfter(shiftStart) ? AttendanceStatus.LATE : AttendanceStatus.ON_TIME;
    }

    public AttendanceStatus computeFinalStatus(Attendance attendance, WorkSchedule schedule) {
        if (schedule == null || schedule.getShift() == null) {
            return attendance.getStatus();
        }
        LocalDateTime shiftStartAt = LocalDateTime.of(schedule.getWorkDate(), schedule.getShift().getStartTime());
        LocalDateTime shiftEndAt = LocalDateTime.of(schedule.getWorkDate(), schedule.getShift().getEndTime());
        if (!schedule.getShift().getEndTime().isAfter(schedule.getShift().getStartTime())) {
            shiftEndAt = shiftEndAt.plusDays(1);
        }

        boolean late = attendance.getCheckInAt() != null && attendance.getCheckInAt().isAfter(shiftStartAt);
        boolean early = attendance.getCheckOutAt() != null && attendance.getCheckOutAt().isBefore(shiftEndAt);

        if (late && early) return AttendanceStatus.LATE_AND_EARLY;
        if (late) return AttendanceStatus.LATE;
        if (early) return AttendanceStatus.EARLY_LEAVE;
        return AttendanceStatus.ON_TIME;
    }

    private java.util.Optional<Attendance> findOpenAttendanceForCheckout(Long employeeId, LocalDate today) {
        java.util.Optional<Attendance> todayAttendance = attendanceRepository.findByEmployeeAndDate(employeeId, today);
        if (todayAttendance.isPresent() && todayAttendance.get().getCheckOutAt() == null) {
            return todayAttendance;
        }

        java.util.Optional<Attendance> yesterdayAttendance = attendanceRepository.findByEmployeeAndDate(employeeId, today.minusDays(1));
        if (yesterdayAttendance.isEmpty()) {
            return java.util.Optional.empty();
        }
        Attendance attendance = yesterdayAttendance.get();
        if (attendance.getCheckInAt() == null || attendance.getCheckOutAt() != null) {
            return java.util.Optional.empty();
        }
        WorkSchedule schedule = attendance.getSchedule();
        if (schedule == null || schedule.getShift() == null) {
            return java.util.Optional.empty();
        }
        boolean overnight = !schedule.getShift().getEndTime().isAfter(schedule.getShift().getStartTime());
        return overnight ? java.util.Optional.of(attendance) : java.util.Optional.empty();
    }
}
