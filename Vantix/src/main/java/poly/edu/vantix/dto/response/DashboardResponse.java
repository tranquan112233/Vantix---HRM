package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long totalEmployees;
    private long totalDepartments;
    private long totalPositions;
    private long activeUsers;
    private long totalTasks;
    private long openTasks;
    private long completedTasks;
    private long overdueTasks;
    private long pendingLeaveRequests;
    private long todayAttendanceRecords;
    private long todayLateRecords;
    private long todayAbsentRecords;
    private long activeContracts;
    private long expiringContracts;
    private long currentPayrollRows;
    private String currentPayrollStatus;
    private Map<String, Long> employeesByStatus;
    private Map<String, Long> employeesByDepartment;
    private Map<String, Long> tasksByStatus;
    private Map<String, Long> leaveRequestsByStatus;
    private Map<String, Long> todayAttendanceByStatus;
    private List<DailyAttendance> attendanceLast7Days;
    private List<RecentEmployee> recentEmployees;
    private List<RecentTask> recentTasks;
    private List<UpcomingLeaveRequest> upcomingLeaveRequests;
    private List<ContractAlert> expiringContractAlerts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentEmployee {
        private Long id;
        private String employeeCode;
        private String fullName;
        private String departmentName;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyAttendance {
        private LocalDate date;
        private long total;
        private long onTime;
        private long late;
        private long absent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentTask {
        private Long id;
        private String title;
        private String assigneeName;
        private String status;
        private LocalDate dueDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingLeaveRequest {
        private Long id;
        private String employeeName;
        private String departmentName;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractAlert {
        private Long id;
        private String contractCode;
        private String employeeName;
        private String departmentName;
        private LocalDate endDate;
    }
}
