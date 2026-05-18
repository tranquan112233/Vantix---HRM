package poly.edu.vantix.service;

import org.springframework.stereotype.Service;
import poly.edu.vantix.dto.response.DashboardResponse;
import poly.edu.vantix.entity.Attendance;
import poly.edu.vantix.entity.Contract;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.LeaveRequest;
import poly.edu.vantix.entity.PayrollPeriod;
import poly.edu.vantix.entity.WorkTask;
import poly.edu.vantix.entity.enums.AttendanceStatus;
import poly.edu.vantix.entity.enums.ContractStatus;
import poly.edu.vantix.entity.enums.EmploymentStatus;
import poly.edu.vantix.entity.enums.LeaveRequestStatus;
import poly.edu.vantix.entity.enums.TaskStatus;
import poly.edu.vantix.entity.enums.UserStatus;
import poly.edu.vantix.repository.AttendanceRepository;
import poly.edu.vantix.repository.ContractRepository;
import poly.edu.vantix.repository.DepartmentRepository;
import poly.edu.vantix.repository.EmployeeRepository;
import poly.edu.vantix.repository.LeaveRequestRepository;
import poly.edu.vantix.repository.PayrollPeriodRepository;
import poly.edu.vantix.repository.PayrollRepository;
import poly.edu.vantix.repository.PositionRepository;
import poly.edu.vantix.repository.TaskRepository;
import poly.edu.vantix.repository.UserRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRepository attendanceRepository;
    private final ContractRepository contractRepository;
    private final ContractService contractService;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayrollRepository payrollRepository;

    public DashboardService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            PositionRepository positionRepository,
            UserRepository userRepository,
            TaskRepository taskRepository,
            LeaveRequestRepository leaveRequestRepository,
            AttendanceRepository attendanceRepository,
            ContractRepository contractRepository,
            ContractService contractService,
            PayrollPeriodRepository payrollPeriodRepository,
            PayrollRepository payrollRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.attendanceRepository = attendanceRepository;
        this.contractRepository = contractRepository;
        this.contractService = contractService;
        this.payrollPeriodRepository = payrollPeriodRepository;
        this.payrollRepository = payrollRepository;
    }

    public DashboardResponse getStats() {
        contractService.expireElapsedActiveContracts();
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);
        List<Employee> employees = employeeRepository.search(null, null, null, null);
        List<WorkTask> tasks = taskRepository.search(null, null, null);
        List<LeaveRequest> leaveRequests = leaveRequestRepository.search(
                null,
                null,
                null,
                null,
                LocalDate.of(today.getYear(), 1, 1),
                LocalDate.of(today.getYear(), 12, 31)
        );
        List<Attendance> todayAttendances = attendanceRepository.search(today, today, null);
        List<Attendance> recentAttendances = attendanceRepository.search(sevenDaysAgo, today, null);
        List<Contract> expiringContracts = contractRepository.findExpiringContracts(today, today.plusDays(30));
        PayrollPeriod currentPayrollPeriod = payrollPeriodRepository
                .findByYearAndMonthAndDeletedFalse(today.getYear(), today.getMonthValue())
                .orElse(null);

        Map<String, Long> employeesByStatus = new LinkedHashMap<>();
        for (EmploymentStatus status : EmploymentStatus.values()) {
            long count = employeeRepository.countByDeletedFalseAndStatus(status);
            if (count > 0) {
                employeesByStatus.put(status.name(), count);
            }
        }

        Map<String, Long> employeesByDepartment = employees.stream()
                .collect(Collectors.groupingBy(
                        employee -> employee.getDepartment() == null ? "Chưa có phòng ban" : employee.getDepartment().getName(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        Map<String, Long> tasksByStatus = enumCounts(tasks, task -> task.getStatus() == null ? null : task.getStatus().name(), TaskStatus.values());
        Map<String, Long> leaveRequestsByStatus = enumCounts(leaveRequests, request -> request.getStatus() == null ? null : request.getStatus().name(), LeaveRequestStatus.values());
        Map<String, Long> todayAttendanceByStatus = enumCounts(todayAttendances, attendance -> attendance.getStatus() == null ? null : attendance.getStatus().name(), AttendanceStatus.values());

        List<DashboardResponse.DailyAttendance> attendanceLast7Days = sevenDayAttendance(today, recentAttendances);

        List<Employee> recent = employeeRepository.findRecentEmployees();
        List<DashboardResponse.RecentEmployee> recentList = recent.stream()
                .limit(5)
                .map(e -> DashboardResponse.RecentEmployee.builder()
                        .id(e.getId())
                        .employeeCode(e.getEmployeeCode())
                        .fullName(e.getFullName())
                        .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
                        .status(e.getStatus() != null ? e.getStatus().name() : null)
                        .build())
                .toList();

        List<DashboardResponse.RecentTask> recentTasks = tasks.stream()
                .limit(6)
                .map(task -> DashboardResponse.RecentTask.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .assigneeName(task.getAssignee() == null ? null : task.getAssignee().getFullName())
                        .status(task.getStatus() == null ? null : task.getStatus().name())
                        .dueDate(task.getDueDate())
                        .build())
                .toList();

        List<DashboardResponse.UpcomingLeaveRequest> upcomingLeaveRequests = leaveRequests.stream()
                .filter(request -> request.getStartDate() != null)
                .filter(request -> !request.getStartDate().isBefore(today))
                .sorted(Comparator.comparing(LeaveRequest::getStartDate))
                .limit(5)
                .map(request -> DashboardResponse.UpcomingLeaveRequest.builder()
                        .id(request.getId())
                        .employeeName(request.getEmployee() == null ? null : request.getEmployee().getFullName())
                        .departmentName(request.getEmployee() == null || request.getEmployee().getDepartment() == null
                                ? null
                                : request.getEmployee().getDepartment().getName())
                        .status(request.getStatus() == null ? null : request.getStatus().name())
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .build())
                .toList();

        List<DashboardResponse.ContractAlert> expiringContractAlerts = expiringContracts.stream()
                .limit(5)
                .map(contract -> DashboardResponse.ContractAlert.builder()
                        .id(contract.getId())
                        .contractCode(contract.getContractCode())
                        .employeeName(contract.getEmployee() == null ? null : contract.getEmployee().getFullName())
                        .departmentName(contract.getEmployee() == null || contract.getEmployee().getDepartment() == null
                                ? null
                                : contract.getEmployee().getDepartment().getName())
                        .endDate(contract.getEndDate())
                        .build())
                .toList();

        long completedTasks = tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).count();
        long overdueTasks = tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(today))
                .count();

        return DashboardResponse.builder()
                .totalEmployees(employeeRepository.countByDeletedFalse())
                .totalDepartments(departmentRepository.countByDeletedFalse())
                .totalPositions(positionRepository.countByDeletedFalse())
                .activeUsers(userRepository.countByDeletedFalseAndStatus(UserStatus.ACTIVE))
                .totalTasks(tasks.size())
                .openTasks(tasks.size() - completedTasks)
                .completedTasks(completedTasks)
                .overdueTasks(overdueTasks)
                .pendingLeaveRequests(leaveRequests.stream().filter(request -> request.getStatus() == LeaveRequestStatus.PENDING).count())
                .todayAttendanceRecords(todayAttendances.size())
                .todayLateRecords(todayAttendances.stream()
                        .filter(attendance -> attendance.getStatus() == AttendanceStatus.LATE
                                || attendance.getStatus() == AttendanceStatus.LATE_AND_EARLY)
                        .count())
                .todayAbsentRecords(todayAttendances.stream().filter(attendance -> attendance.getStatus() == AttendanceStatus.ABSENT).count())
                .activeContracts(contractRepository.countByDeletedFalseAndStatus(ContractStatus.ACTIVE))
                .expiringContracts(expiringContracts.size())
                .currentPayrollRows(currentPayrollPeriod == null ? 0 : payrollRepository.countByPeriodIdAndDeletedFalse(currentPayrollPeriod.getId()))
                .currentPayrollStatus(currentPayrollPeriod == null ? null : currentPayrollPeriod.getStatus().name())
                .employeesByStatus(employeesByStatus)
                .employeesByDepartment(employeesByDepartment)
                .tasksByStatus(tasksByStatus)
                .leaveRequestsByStatus(leaveRequestsByStatus)
                .todayAttendanceByStatus(todayAttendanceByStatus)
                .attendanceLast7Days(attendanceLast7Days)
                .recentEmployees(recentList)
                .recentTasks(recentTasks)
                .upcomingLeaveRequests(upcomingLeaveRequests)
                .expiringContractAlerts(expiringContractAlerts)
                .build();
    }

    private <T, E extends Enum<E>> Map<String, Long> enumCounts(List<T> values, Function<T, String> classifier, E[] enumValues) {
        Map<String, Long> rawCounts = values.stream()
                .map(classifier)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<String, Long> ordered = new LinkedHashMap<>();
        for (E enumValue : enumValues) {
            long count = rawCounts.getOrDefault(enumValue.name(), 0L);
            if (count > 0) {
                ordered.put(enumValue.name(), count);
            }
        }
        return ordered;
    }

    private List<DashboardResponse.DailyAttendance> sevenDayAttendance(LocalDate today, List<Attendance> attendances) {
        Map<LocalDate, List<Attendance>> byDate = attendances.stream()
                .filter(attendance -> attendance.getWorkDate() != null)
                .collect(Collectors.groupingBy(Attendance::getWorkDate));

        return today.minusDays(6)
                .datesUntil(today.plusDays(1))
                .map(date -> {
                    List<Attendance> day = byDate.getOrDefault(date, List.of());
                    return DashboardResponse.DailyAttendance.builder()
                            .date(date)
                            .total(day.size())
                            .onTime(day.stream().filter(attendance -> attendance.getStatus() == AttendanceStatus.ON_TIME).count())
                            .late(day.stream().filter(attendance -> attendance.getStatus() == AttendanceStatus.LATE
                                    || attendance.getStatus() == AttendanceStatus.LATE_AND_EARLY).count())
                            .absent(day.stream().filter(attendance -> attendance.getStatus() == AttendanceStatus.ABSENT).count())
                            .build();
                })
                .toList();
    }
}
