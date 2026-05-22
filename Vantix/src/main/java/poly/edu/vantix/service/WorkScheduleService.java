package poly.edu.vantix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.WorkScheduleBulkRequest;
import poly.edu.vantix.dto.request.WorkScheduleRequest;
import poly.edu.vantix.dto.response.WorkScheduleBulkResponse;
import poly.edu.vantix.dto.response.WorkScheduleResponse;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.Shift;
import poly.edu.vantix.entity.WorkLocation;
import poly.edu.vantix.entity.WorkSchedule;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.repository.EmployeeRepository;
import poly.edu.vantix.repository.ShiftRepository;
import poly.edu.vantix.repository.WorkLocationRepository;
import poly.edu.vantix.repository.WorkScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WorkScheduleService {

    private final WorkScheduleRepository scheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final WorkLocationRepository locationRepository;
    private final BusinessCalendarService businessCalendarService;

    public WorkScheduleService(
            WorkScheduleRepository scheduleRepository,
            EmployeeRepository employeeRepository,
            ShiftRepository shiftRepository,
            WorkLocationRepository locationRepository,
            BusinessCalendarService businessCalendarService
    ) {
        this.scheduleRepository = scheduleRepository;
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
        this.locationRepository = locationRepository;
        this.businessCalendarService = businessCalendarService;
    }

    @Transactional(readOnly = true)
    public List<WorkScheduleResponse> search(Employee actor, LocalDate from, LocalDate to, Long employeeId, Long departmentId) {
        Long scopedDepartmentId = resolveScopedDepartmentId(actor, departmentId);
        Long scopedEmployeeId = employeeId;
        if (employeeId != null) {
            Employee employee = employeeRepository.findActiveById(employeeId)
                    .orElseThrow(() -> new BusinessException("employeeId", "Employee does not exist"));
            ensureSchedulableByActor(actor, employee);
            scopedEmployeeId = employee.getId();
        }

        return scheduleRepository.search(from, to, scopedEmployeeId, scopedDepartmentId).stream()
                .map(WorkScheduleResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<WorkScheduleResponse> findByEmployeeAndDate(Long employeeId, LocalDate date) {
        return scheduleRepository.findByEmployeeAndDate(employeeId, date)
                .map(WorkScheduleResponse::fromEntity);
    }

    @Transactional
    public WorkScheduleResponse create(Employee actor, WorkScheduleRequest request) {
        if (scheduleRepository.existsByEmployeeIdAndWorkDateAndDeletedFalse(
                request.getEmployeeId(), request.getWorkDate())) {
            throw new BusinessException("employeeId",
                    "Employee already has a schedule on this date");
        }
        WorkSchedule schedule = new WorkSchedule();
        applyRequest(actor, request, schedule);
        return WorkScheduleResponse.fromEntity(scheduleRepository.save(schedule));
    }

    @Transactional
    public WorkScheduleResponse update(Employee actor, Long id, WorkScheduleRequest request) {
        WorkSchedule schedule = scheduleRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Schedule does not exist"));
        ensureSchedulableByActor(actor, schedule.getEmployee());

        boolean changed = !schedule.getEmployee().getId().equals(request.getEmployeeId())
                || !schedule.getWorkDate().equals(request.getWorkDate());
        if (changed && scheduleRepository.existsByEmployeeIdAndWorkDateAndDeletedFalse(
                request.getEmployeeId(), request.getWorkDate())) {
            throw new BusinessException("employeeId",
                    "Employee already has a schedule on this date");
        }
        applyRequest(actor, request, schedule);
        return WorkScheduleResponse.fromEntity(scheduleRepository.save(schedule));
    }

    @Transactional
    public WorkScheduleBulkResponse bulkCreate(Employee actor, WorkScheduleBulkRequest request) {
        if (request.getFromDate().isAfter(request.getToDate())) {
            throw new BusinessException("toDate", "End date must be on or after start date");
        }

        Shift shift = shiftRepository.findActiveById(request.getShiftId())
                .orElseThrow(() -> new BusinessException("shiftId", "Shift does not exist"));

        WorkLocation location = null;
        if (request.getLocationId() != null) {
            location = locationRepository.findActiveById(request.getLocationId())
                    .orElseThrow(() -> new BusinessException("locationId", "Location does not exist"));
        }

        Map<Long, Employee> employeeMap = new LinkedHashMap<>();
        if (request.getEmployeeIds() != null) {
            for (Long id : request.getEmployeeIds()) {
                employeeRepository.findActiveById(id).ifPresent(e -> {
                    ensureSchedulableByActor(actor, e);
                    employeeMap.put(e.getId(), e);
                });
            }
        }
        if (request.getDepartmentIds() != null) {
            for (Long deptId : request.getDepartmentIds()) {
                ensureDepartmentScope(actor, deptId);
                for (Employee e : employeeRepository.findActiveByDepartmentId(deptId)) {
                    ensureSchedulableByActor(actor, e);
                    employeeMap.putIfAbsent(e.getId(), e);
                }
            }
        }

        if (employeeMap.isEmpty()) {
            throw new BusinessException("employeeIds", "Select at least one employee or department");
        }

        EnumSet<java.time.DayOfWeek> daysOfWeek = EnumSet.copyOf(request.getDaysOfWeek());
        boolean skipExisting = request.getSkipExisting() == null || request.getSkipExisting();
        boolean skipPublicHolidays = request.getSkipPublicHolidays() == null || request.getSkipPublicHolidays();

        int created = 0;
        int skipped = 0;
        List<WorkSchedule> toSave = new ArrayList<>();

        for (Employee employee : employeeMap.values()) {
            LocalDate date = request.getFromDate();
            while (!date.isAfter(request.getToDate())) {
                if (daysOfWeek.contains(date.getDayOfWeek())) {
                    if (skipPublicHolidays && businessCalendarService.isPublicHoliday(date)) {
                        skipped++;
                        date = date.plusDays(1);
                        continue;
                    }
                    boolean exists = scheduleRepository
                            .existsByEmployeeIdAndWorkDateAndDeletedFalse(employee.getId(), date);
                    if (exists) {
                        if (skipExisting) {
                            skipped++;
                        } else {
                            throw new BusinessException(String.format(
                                    "Employee %s already has a schedule on %s",
                                    employee.getFullName(), date));
                        }
                    } else {
                        WorkSchedule schedule = new WorkSchedule();
                        schedule.setEmployee(employee);
                        schedule.setShift(shift);
                        schedule.setLocation(location);
                        schedule.setWorkDate(date);
                        schedule.setNote(request.getNote());
                        toSave.add(schedule);
                        created++;
                    }
                }
                date = date.plusDays(1);
            }
        }

        scheduleRepository.saveAll(toSave);

        return WorkScheduleBulkResponse.builder()
                .created(created)
                .skipped(skipped)
                .employeesProcessed(employeeMap.size())
                .build();
    }

    @Transactional
    public void delete(Employee actor, Long id) {
        WorkSchedule schedule = scheduleRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Schedule does not exist"));
        ensureSchedulableByActor(actor, schedule.getEmployee());
        schedule.setDeleted(true);
        schedule.setDeletedAt(LocalDateTime.now());
        scheduleRepository.save(schedule);
    }

    private void applyRequest(Employee actor, WorkScheduleRequest request, WorkSchedule schedule) {
        Employee employee = employeeRepository.findActiveById(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException("employeeId", "Employee does not exist"));
        ensureSchedulableByActor(actor, employee);
        Shift shift = shiftRepository.findActiveById(request.getShiftId())
                .orElseThrow(() -> new BusinessException("shiftId", "Shift does not exist"));

        WorkLocation location = null;
        if (request.getLocationId() != null) {
            location = locationRepository.findActiveById(request.getLocationId())
                    .orElseThrow(() -> new BusinessException("locationId", "Location does not exist"));
        }

        schedule.setEmployee(employee);
        schedule.setShift(shift);
        schedule.setLocation(location);
        schedule.setWorkDate(request.getWorkDate());
        schedule.setNote(request.getNote());
    }

    private void ensureDepartmentScope(Employee actor, Long departmentId) {
        if (isAdmin(actor)) {
            return;
        }
        Long actorDepartmentId = actor != null && actor.getDepartment() != null ? actor.getDepartment().getId() : null;
        if (actorDepartmentId == null) {
            throw new BusinessException("Current employee is not assigned to a department");
        }
        if (departmentId == null || !actorDepartmentId.equals(departmentId)) {
            throw new BusinessException("departmentIds", "You can only manage schedules for employees in your department");
        }
    }

    private void ensureSchedulableByActor(Employee actor, Employee target) {
        if (isAdmin(actor)) {
            return;
        }
        Long actorDepartmentId = actor != null && actor.getDepartment() != null ? actor.getDepartment().getId() : null;
        if (actorDepartmentId == null) {
            throw new BusinessException("Current employee is not assigned to a department");
        }

        Long targetDepartmentId = target != null && target.getDepartment() != null ? target.getDepartment().getId() : null;
        if (!actorDepartmentId.equals(targetDepartmentId)) {
            throw new BusinessException("employeeId", "You can only manage schedules for employees in your department");
        }
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
            throw new BusinessException("departmentId", "You can only view schedules for employees in your department");
        }
        return actorDepartmentId;
    }

    private boolean isAdmin(Employee actor) {
        return actor != null
                && actor.getUser() != null
                && actor.getUser().getRole() != null
                && "Admin".equalsIgnoreCase(actor.getUser().getRole().getName());
    }
}
