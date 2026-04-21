package poly.edu.vantix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.entity.Attendance;
import poly.edu.vantix.entity.Contract;
import poly.edu.vantix.entity.Department;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.LeaveRequest;
import poly.edu.vantix.entity.Notification;
import poly.edu.vantix.entity.Payroll;
import poly.edu.vantix.entity.PayrollPeriod;
import poly.edu.vantix.entity.Permission;
import poly.edu.vantix.entity.Position;
import poly.edu.vantix.entity.PublicHoliday;
import poly.edu.vantix.entity.Role;
import poly.edu.vantix.entity.Shift;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.entity.WorkLocation;
import poly.edu.vantix.entity.WorkSchedule;
import poly.edu.vantix.entity.WorkTask;
import poly.edu.vantix.entity.enums.AttendanceStatus;
import poly.edu.vantix.entity.enums.ContractStatus;
import poly.edu.vantix.entity.enums.ContractType;
import poly.edu.vantix.entity.enums.EmploymentStatus;
import poly.edu.vantix.entity.enums.Gender;
import poly.edu.vantix.entity.enums.LeaveDayUnit;
import poly.edu.vantix.entity.enums.LeaveRequestStatus;
import poly.edu.vantix.entity.enums.LeaveType;
import poly.edu.vantix.entity.enums.NotificationStatus;
import poly.edu.vantix.entity.enums.NotificationType;
import poly.edu.vantix.entity.enums.PayrollStatus;
import poly.edu.vantix.entity.enums.TaskStatus;
import poly.edu.vantix.entity.enums.UserStatus;
import poly.edu.vantix.repository.AttendanceRepository;
import poly.edu.vantix.repository.ContractRepository;
import poly.edu.vantix.repository.DepartmentRepository;
import poly.edu.vantix.repository.EmployeeRepository;
import poly.edu.vantix.repository.LeaveRequestRepository;
import poly.edu.vantix.repository.NotificationRepository;
import poly.edu.vantix.repository.PayrollPeriodRepository;
import poly.edu.vantix.repository.PayrollRepository;
import poly.edu.vantix.repository.PermissionRepository;
import poly.edu.vantix.repository.PositionRepository;
import poly.edu.vantix.repository.PublicHolidayRepository;
import poly.edu.vantix.repository.RoleRepository;
import poly.edu.vantix.repository.ShiftRepository;
import poly.edu.vantix.repository.TaskRepository;
import poly.edu.vantix.repository.UserRepository;
import poly.edu.vantix.repository.WorkLocationRepository;
import poly.edu.vantix.repository.WorkScheduleRepository;
import poly.edu.vantix.util.PayrollCalculation;
import poly.edu.vantix.util.PayrollInput;
import poly.edu.vantix.util.VietnamPayrollCalculator;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class DataSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ShiftRepository shiftRepository;
    private final WorkLocationRepository workLocationRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationRepository notificationRepository;
    private final ContractRepository contractRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayrollRepository payrollRepository;
    private final PublicHolidayRepository publicHolidayRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin.email:admin@vantix.local}")
    private String adminEmail;

    @Value("${app.seed.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${app.seed.sample-employee-count:120}")
    private int sampleEmployeeCount;

    @Value("${app.seed.schedule-employee-count:30}")
    private int scheduleEmployeeCount;

    public DataSeeder(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            PositionRepository positionRepository,
            EmployeeRepository employeeRepository,
            TaskRepository taskRepository,
            ShiftRepository shiftRepository,
            WorkLocationRepository workLocationRepository,
            WorkScheduleRepository workScheduleRepository,
            AttendanceRepository attendanceRepository,
            LeaveRequestRepository leaveRequestRepository,
            NotificationRepository notificationRepository,
            ContractRepository contractRepository,
            PayrollPeriodRepository payrollPeriodRepository,
            PayrollRepository payrollRepository,
            PublicHolidayRepository publicHolidayRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.employeeRepository = employeeRepository;
        this.taskRepository = taskRepository;
        this.shiftRepository = shiftRepository;
        this.workLocationRepository = workLocationRepository;
        this.workScheduleRepository = workScheduleRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.notificationRepository = notificationRepository;
        this.contractRepository = contractRepository;
        this.payrollPeriodRepository = payrollPeriodRepository;
        this.payrollRepository = payrollRepository;
        this.publicHolidayRepository = publicHolidayRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        Map<String, Permission> permissions = seedPermissions();

        Role adminRole = seedRole("Admin", "Quản trị hệ thống", permissions.values());

        Role hrRole = seedRole(
                "HR",
                "Phòng nhân sự",
                permissionsByName(permissions, List.of(
                        "EMPLOYEE_VIEW",
                        "EMPLOYEE_CREATE",
                        "EMPLOYEE_UPDATE",
                        "DEPARTMENT_VIEW",
                        "POSITION_VIEW",
                        "TASK_VIEW",
                        "TASK_CREATE",
                        "TASK_UPDATE",
                        "TASK_DELETE",
                        "LEAVE_REQUEST_VIEW",
                        "LEAVE_REQUEST_CREATE",
                        "LEAVE_REQUEST_UPDATE",
                        "LEAVE_REQUEST_CANCEL",
                        "LEAVE_REQUEST_VIEW_ALL",
                        "LEAVE_REQUEST_APPROVE",
                        "NOTIFICATION_VIEW",
                        "NOTIFICATION_SEND",
                        "WORK_LOCATION_VIEW",
                        "WORK_LOCATION_CREATE",
                        "WORK_LOCATION_UPDATE",
                        "WORK_LOCATION_DELETE",
                        "SHIFT_VIEW",
                        "SHIFT_CREATE",
                        "SHIFT_UPDATE",
                        "SHIFT_DELETE",
                        "SCHEDULE_VIEW_ALL",
                        "SCHEDULE_CREATE",
                        "SCHEDULE_UPDATE",
                        "SCHEDULE_DELETE",
                        "ATTENDANCE_VIEW_ALL",
                        "CONTRACT_VIEW",
                        "CONTRACT_CREATE",
                        "CONTRACT_UPDATE",
                        "CONTRACT_DELETE",
                        "PAYROLL_VIEW",
                        "PAYROLL_CREATE",
                        "PAYROLL_UPDATE",
                        "PAYROLL_APPROVE"
                ))
        );

        Role employeeRole = seedRole(
                "Employee",
                "Nhân viên",
                permissionsByName(permissions, List.of(
                        "EMPLOYEE_VIEW",
                        "TASK_VIEW",
                        "LEAVE_REQUEST_VIEW",
                        "LEAVE_REQUEST_CREATE",
                        "LEAVE_REQUEST_UPDATE",
                        "LEAVE_REQUEST_CANCEL",
                        "NOTIFICATION_VIEW",
                        "SHIFT_VIEW"
                ))
        );

        Role managerRole = seedRole(
                "Manager",
                "Quản lý bộ phận",
                permissionsByName(permissions, List.of(
                        "EMPLOYEE_VIEW",
                        "DEPARTMENT_VIEW",
                        "POSITION_VIEW",
                        "TASK_VIEW",
                        "TASK_CREATE",
                        "TASK_UPDATE",
                        "TASK_DELETE",
                        "LEAVE_REQUEST_VIEW",
                        "LEAVE_REQUEST_CREATE",
                        "LEAVE_REQUEST_UPDATE",
                        "LEAVE_REQUEST_CANCEL",
                        "LEAVE_REQUEST_VIEW_ALL",
                        "LEAVE_REQUEST_APPROVE",
                        "NOTIFICATION_VIEW",
                        "SCHEDULE_VIEW_ALL",
                        "SHIFT_VIEW",
                        "ATTENDANCE_VIEW_ALL",
                        "CONTRACT_VIEW",
                        "PAYROLL_VIEW",
                        "PAYROLL_APPROVE"
                ))
        );

        Role schedulerRole = seedRole(
                "Scheduler",
                "Điều phối lịch làm việc",
                permissionsByName(permissions, List.of(
                        "EMPLOYEE_VIEW",
                        "DEPARTMENT_VIEW",
                        "POSITION_VIEW",
                        "NOTIFICATION_VIEW",
                        "WORK_LOCATION_VIEW",
                        "WORK_LOCATION_CREATE",
                        "WORK_LOCATION_UPDATE",
                        "WORK_LOCATION_DELETE",
                        "SHIFT_VIEW",
                        "SHIFT_CREATE",
                        "SHIFT_UPDATE",
                        "SHIFT_DELETE",
                        "SCHEDULE_VIEW_ALL",
                        "SCHEDULE_CREATE",
                        "SCHEDULE_UPDATE",
                        "SCHEDULE_DELETE",
                        "ATTENDANCE_VIEW_ALL"
                ))
        );

        Role notifierRole = seedRole(
                "Notifier",
                "Gửi thông báo nội bộ",
                permissionsByName(permissions, List.of(
                        "USER_VIEW",
                        "EMPLOYEE_VIEW",
                        "NOTIFICATION_VIEW",
                        "NOTIFICATION_SEND"
                ))
        );

        Role auditorRole = seedRole(
                "Auditor",
                "Chỉ xem dữ liệu và nhật ký",
                permissionsByName(permissions, List.of(
                        "USER_VIEW",
                        "ROLE_VIEW",
                        "EMPLOYEE_VIEW",
                        "DEPARTMENT_VIEW",
                        "POSITION_VIEW",
                        "TASK_VIEW",
                        "LEAVE_REQUEST_VIEW",
                        "LEAVE_REQUEST_VIEW_ALL",
                        "NOTIFICATION_VIEW",
                        "SCHEDULE_VIEW_ALL",
                        "SHIFT_VIEW",
                        "WORK_LOCATION_VIEW",
                        "ATTENDANCE_VIEW_ALL",
                        "SYSTEM_LOG_VIEW",
                        "CONTRACT_VIEW",
                        "PAYROLL_VIEW"
                ))
        );

        User adminUser = seedAdminUser(adminRole);
        Map<String, Department> departments = seedDepartments();
        Map<String, Position> positions = seedPositions(departments);

        seedAdminEmployee(adminUser, departments.get("ADMIN"), positions.get("SYS_ADMIN"));
        seedNamedTestAccounts(departments, positions, hrRole, managerRole, employeeRole, schedulerRole, notifierRole, auditorRole);
        seedSampleEmployees(sampleEmployeeCount, departments, positions, hrRole, employeeRole);
        seedDepartmentHeads(departments);
        seedSampleTasks();

        Map<String, Shift> shifts = seedShifts();
        Map<String, WorkLocation> locations = seedWorkLocations();
        seedPublicHolidays();
        seedWorkSchedules(shifts, locations);
        seedAttendances();
        seedLeaveRequests(adminUser);
        seedContracts();
        seedPayrollSamples(adminUser);
        seedNotifications(adminUser);
    }

    // ===================== PERMISSION =====================

    private Map<String, Permission> seedPermissions() {
        Map<String, Permission> permissions = new LinkedHashMap<>();

        defaultPermissions().forEach((name, description) -> {
            Permission permission = permissionRepository.findByName(name).orElseGet(Permission::new);
            permission.setName(name);
            permission.setDescription(description);
            permissions.put(name, permissionRepository.save(permission));
        });

        return permissions;
    }

    private Map<String, String> defaultPermissions() {
        Map<String, String> permissions = new LinkedHashMap<>();

        permissions.put("USER_VIEW", "Xem người dùng");
        permissions.put("USER_CREATE", "Tạo người dùng");
        permissions.put("USER_UPDATE", "Cập nhật người dùng");
        permissions.put("USER_DELETE", "Xóa người dùng");

        permissions.put("ROLE_VIEW", "Xem vai trò");
        permissions.put("ROLE_CREATE", "Tạo vai trò");
        permissions.put("ROLE_UPDATE", "Cập nhật vai trò");
        permissions.put("ROLE_DELETE", "Xóa vai trò");

        permissions.put("EMPLOYEE_VIEW", "Xem nhân viên");
        permissions.put("EMPLOYEE_CREATE", "Tạo nhân viên");
        permissions.put("EMPLOYEE_UPDATE", "Cập nhật nhân viên");
        permissions.put("EMPLOYEE_DELETE", "Xóa nhân viên");

        permissions.put("DEPARTMENT_VIEW", "Xem phòng ban");
        permissions.put("DEPARTMENT_CREATE", "Tạo phòng ban");
        permissions.put("DEPARTMENT_UPDATE", "Cập nhật phòng ban");
        permissions.put("DEPARTMENT_DELETE", "Xóa phòng ban");

        permissions.put("POSITION_VIEW", "Xem chức vụ");
        permissions.put("POSITION_CREATE", "Tạo chức vụ");
        permissions.put("POSITION_UPDATE", "Cập nhật chức vụ");
        permissions.put("POSITION_DELETE", "Xóa chức vụ");

        permissions.put("NOTIFICATION_VIEW", "Xem thông báo");
        permissions.put("NOTIFICATION_SEND", "Gửi thông báo");

        permissions.put("TASK_VIEW", "Xem công việc");
        permissions.put("TASK_CREATE", "Tạo công việc");
        permissions.put("TASK_UPDATE", "Cập nhật công việc");
        permissions.put("TASK_DELETE", "Xóa công việc");

        permissions.put("LEAVE_REQUEST_VIEW", "Xem đơn xin nghỉ của mình");
        permissions.put("LEAVE_REQUEST_CREATE", "Tạo đơn xin nghỉ");
        permissions.put("LEAVE_REQUEST_UPDATE", "Cập nhật đơn xin nghỉ của mình");
        permissions.put("LEAVE_REQUEST_CANCEL", "Hủy đơn xin nghỉ của mình");
        permissions.put("LEAVE_REQUEST_VIEW_ALL", "Xem tất cả đơn xin nghỉ");
        permissions.put("LEAVE_REQUEST_APPROVE", "Duyệt hoặc từ chối đơn xin nghỉ");
        permissions.put("LEAVE_REQUEST_MANAGE", "Quản lý toàn bộ đơn xin nghỉ");

        permissions.put("SYSTEM_LOG_VIEW", "Xem nhật ký hệ thống");

        permissions.put("WORK_LOCATION_VIEW", "Xem địa điểm làm việc");
        permissions.put("WORK_LOCATION_CREATE", "Tạo địa điểm làm việc");
        permissions.put("WORK_LOCATION_UPDATE", "Cập nhật địa điểm làm việc");
        permissions.put("WORK_LOCATION_DELETE", "Xóa địa điểm làm việc");

        permissions.put("SHIFT_VIEW", "Xem ca làm việc");
        permissions.put("SHIFT_CREATE", "Tạo ca làm việc");
        permissions.put("SHIFT_UPDATE", "Cập nhật ca làm việc");
        permissions.put("SHIFT_DELETE", "Xóa ca làm việc");

        permissions.put("SCHEDULE_VIEW_ALL", "Xem toàn bộ lịch làm việc");
        permissions.put("SCHEDULE_CREATE", "Tạo lịch làm việc");
        permissions.put("SCHEDULE_UPDATE", "Cập nhật lịch làm việc");
        permissions.put("SCHEDULE_DELETE", "Xóa lịch làm việc");

        permissions.put("ATTENDANCE_VIEW_ALL", "Xem toàn bộ chấm công");

        // === Hợp đồng lao động ===
        permissions.put("CONTRACT_VIEW", "Xem hợp đồng lao động");
        permissions.put("CONTRACT_CREATE", "Tạo hợp đồng lao động");
        permissions.put("CONTRACT_UPDATE", "Cập nhật hợp đồng lao động");
        permissions.put("CONTRACT_DELETE", "Xóa hợp đồng lao động");

        // === Tiền lương ===
        permissions.put("PAYROLL_VIEW", "Xem bảng lương");
        permissions.put("PAYROLL_CREATE", "Tạo kỳ lương và tính lương");
        permissions.put("PAYROLL_UPDATE", "Điều chỉnh bảng lương");
        permissions.put("PAYROLL_DELETE", "Xóa kỳ lương");
        permissions.put("PAYROLL_APPROVE", "Duyệt bảng lương");
        permissions.put("PAYROLL_PAY", "Đánh dấu đã chi trả lương");
        permissions.put("PAYROLL_MANAGE", "Quản lý toàn bộ bảng lương");

        return permissions;
    }

    private List<Permission> permissionsByName(Map<String, Permission> permissions, List<String> names) {
        return names.stream()
                .map(permissions::get)
                .toList();
    }

    // ===================== ROLE =====================

    private Role seedRole(String name, String description, Collection<Permission> permissions) {
        Role role = roleRepository.findByName(name).orElseGet(Role::new);
        role.setName(name);
        role.setDescription(description);
        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);
        return roleRepository.save(role);
    }

    // ===================== USER =====================

    private User seedAdminUser(Role adminRole) {
        return userRepository.findByUsername(adminUsername).orElseGet(() -> {
            User user = new User();
            user.setUsername(adminUsername);
            user.setEmail(adminEmail);
            user.setPassword(passwordEncoder.encode(adminPassword));
            user.setRole(adminRole);
            user.setStatus(UserStatus.ACTIVE);
            return userRepository.save(user);
        });
    }

    private User seedSampleUser(int index, Role role) {
        String number = "%03d".formatted(index);
        String username = "user" + number;
        String email = "user" + number + "@vantix.vn";

        User user = userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(passwordEncoder.encode("User@123"));
            return newUser;
        });

        user.setEmail(email);
        user.setRole(role);
        user.setStatus(index % 17 == 0 ? UserStatus.LOCKED : UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    private User seedTestUser(String username, String email, Role role) {
        User user = userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username);
            return newUser;
        });

        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Test@123"));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    // ===================== DEPARTMENT =====================

    private Map<String, Department> seedDepartments() {
        Map<String, Department> departments = new LinkedHashMap<>();

        departments.put("ADMIN", seedDepartment("ADMIN", "Ban quản trị", "Quản trị hệ thống"));
        departments.put("HR", seedDepartment("HR", "Phòng nhân sự", "Quản lý hồ sơ và nhân sự"));
        departments.put("ENG", seedDepartment("ENG", "Phòng kỹ thuật", "Phát triển và bảo trì hệ thống"));
        departments.put("SALES", seedDepartment("SALES", "Phòng kinh doanh", "Tìm kiếm khách hàng và doanh thu"));
        departments.put("FIN", seedDepartment("FIN", "Phòng tài chính", "Kế toán, lương và chi phí"));
        departments.put("OPS", seedDepartment("OPS", "Phòng vận hành", "Điều phối hoạt động nội bộ"));
        departments.put("MKT", seedDepartment("MKT", "Phòng marketing", "Truyền thông và quảng bá"));

        return departments;
    }

    private Department seedDepartment(String code, String name, String description) {
        Department department = departmentRepository.findByCode(code).orElseGet(Department::new);
        department.setCode(code);
        department.setName(name);
        department.setDescription(description);
        return departmentRepository.save(department);
    }

    // ===================== POSITION =====================

    private Map<String, Position> seedPositions(Map<String, Department> departments) {
        Map<String, Position> positions = new LinkedHashMap<>();

        positions.put("SYS_ADMIN", seedPosition(
                "SYS_ADMIN",
                "Quản trị hệ thống",
                "Quản lý và vận hành hệ thống",
                departments.get("ADMIN")
        ));

        positions.put("HR_MANAGER", seedPosition(
                "HR_MANAGER",
                "Trưởng phòng nhân sự",
                "Quản lý phòng nhân sự",
                departments.get("HR")
        ));

        positions.put("HR_SPECIALIST", seedPosition(
                "HR_SPECIALIST",
                "Chuyên viên nhân sự",
                "Xử lý hồ sơ và nghiệp vụ nhân sự",
                departments.get("HR")
        ));

        positions.put("SOFTWARE_ENGINEER", seedPosition(
                "SOFTWARE_ENGINEER",
                "Lập trình viên",
                "Phát triển phần mềm",
                departments.get("ENG")
        ));

        positions.put("QA_ENGINEER", seedPosition(
                "QA_ENGINEER",
                "Kiểm thử phần mềm",
                "Kiểm thử và đảm bảo chất lượng",
                departments.get("ENG")
        ));

        positions.put("PRODUCT_MANAGER", seedPosition(
                "PRODUCT_MANAGER",
                "Quản lý sản phẩm",
                "Phụ trách kế hoạch và định hướng sản phẩm",
                departments.get("ENG")
        ));

        positions.put("SALES_EXECUTIVE", seedPosition(
                "SALES_EXECUTIVE",
                "Nhân viên kinh doanh",
                "Tư vấn và bán hàng",
                departments.get("SALES")
        ));

        positions.put("ACCOUNTANT", seedPosition(
                "ACCOUNTANT",
                "Kế toán",
                "Quản lý số liệu tài chính",
                departments.get("FIN")
        ));

        positions.put("OPS_COORDINATOR", seedPosition(
                "OPS_COORDINATOR",
                "Điều phối vận hành",
                "Điều phối công việc nội bộ",
                departments.get("OPS")
        ));

        positions.put("MARKETING_SPECIALIST", seedPosition(
                "MARKETING_SPECIALIST",
                "Chuyên viên marketing",
                "Triển khai nội dung và chiến dịch",
                departments.get("MKT")
        ));

        return positions;
    }

    private Position seedPosition(String code, String name, String description, Department department) {
        Position position = positionRepository.findByCode(code).orElseGet(Position::new);
        position.setCode(code);
        position.setName(name);
        position.setDescription(description);
        position.setDepartment(department);
        return positionRepository.save(position);
    }

    // ===================== EMPLOYEE =====================

    private void seedAdminEmployee(User adminUser, Department department, Position position) {
        Employee employee = employeeRepository.findByEmployeeCode("EMP-ADMIN").orElseGet(Employee::new);
        employee.setEmployeeCode("EMP-ADMIN");
        employee.setUser(adminUser);
        employee.setFullName("Quản trị viên hệ thống");
        employee.setGender(Gender.OTHER);
        employee.setPersonalEmail(adminUser.getEmail());
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setJoinDate(LocalDate.now());
        employee.setStatus(EmploymentStatus.ACTIVE);
        employee.setAddress("Trụ sở chính Vantix");
        employee.setPhoneNumber("0900000000");
        employee.setCitizenId("079200000000");
        employee.setBankAccount("9704000000000000");
        employee.setTaxCode("MSTADMIN001");
        employee.setInsuranceNumber("BHADMIN001");
        employee.setEmergencyContactName("Người thân quản trị");
        employee.setEmergencyContactPhone("0910000000");
        employeeRepository.save(employee);
    }

    private void seedNamedTestAccounts(
            Map<String, Department> departments,
            Map<String, Position> positions,
            Role hrRole,
            Role managerRole,
            Role employeeRole,
            Role schedulerRole,
            Role notifierRole,
            Role auditorRole
    ) {
        seedTestEmployee(
                "EMP-TEST-HR",
                "hr_test",
                "hr_test@vantix.local",
                "Trần Thị HR Test",
                Gender.FEMALE,
                departments.get("HR"),
                positions.get("HR_SPECIALIST"),
                hrRole,
                900001
        );

        seedTestEmployee(
                "EMP-TEST-MGR",
                "manager_test",
                "manager_test@vantix.local",
                "Nguyễn Văn Manager Test",
                Gender.MALE,
                departments.get("HR"),
                positions.get("HR_MANAGER"),
                managerRole,
                900002
        );

        seedTestEmployee(
                "EMP-TEST-EMP",
                "employee_test",
                "employee_test@vantix.local",
                "Lê Minh Employee Test",
                Gender.MALE,
                departments.get("ENG"),
                positions.get("SOFTWARE_ENGINEER"),
                employeeRole,
                900003
        );

        seedTestEmployee(
                "EMP-TEST-SCH",
                "scheduler_test",
                "scheduler_test@vantix.local",
                "Phạm Thu Scheduler Test",
                Gender.FEMALE,
                departments.get("OPS"),
                positions.get("OPS_COORDINATOR"),
                schedulerRole,
                900004
        );

        seedTestEmployee(
                "EMP-TEST-NOTI",
                "notifier_test",
                "notifier_test@vantix.local",
                "Hoàng An Notifier Test",
                Gender.OTHER,
                departments.get("MKT"),
                positions.get("MARKETING_SPECIALIST"),
                notifierRole,
                900005
        );

        seedTestEmployee(
                "EMP-TEST-AUD",
                "auditor_test",
                "auditor_test@vantix.local",
                "Đỗ Kim Auditor Test",
                Gender.FEMALE,
                departments.get("FIN"),
                positions.get("ACCOUNTANT"),
                auditorRole,
                900006
        );
    }

    private void seedTestEmployee(
            String employeeCode,
            String username,
            String email,
            String fullName,
            Gender gender,
            Department department,
            Position position,
            Role role,
            int uniqueNumber
    ) {
        User user = seedTestUser(username, email, role);

        Employee employee = employeeRepository.findByEmployeeCode(employeeCode).orElseGet(Employee::new);
        employee.setEmployeeCode(employeeCode);
        employee.setUser(user);
        employee.setFullName(fullName);
        employee.setDateOfBirth(LocalDate.of(1992, (uniqueNumber % 12) + 1, (uniqueNumber % 28) + 1));
        employee.setGender(gender);
        employee.setCitizenId("0792%08d".formatted(uniqueNumber));
        employee.setPhoneNumber("09%08d".formatted(uniqueNumber));
        employee.setPersonalEmail(email);
        employee.setAddress("Tài khoản test - Trụ sở chính Vantix");
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setJoinDate(LocalDate.now().minusMonths(6));
        employee.setTerminationDate(null);
        employee.setStatus(EmploymentStatus.ACTIVE);
        employee.setBankAccount("9704%012d".formatted(uniqueNumber));
        employee.setTaxCode("MST%08d".formatted(uniqueNumber));
        employee.setInsuranceNumber("BHXH%08d".formatted(uniqueNumber));
        employee.setEmergencyContactName("Liên hệ test");
        employee.setEmergencyContactPhone("08%08d".formatted(uniqueNumber));

        employeeRepository.save(employee);
    }

    private void seedSampleEmployees(
            int count,
            Map<String, Department> departments,
            Map<String, Position> positions,
            Role hrRole,
            Role employeeRole
    ) {
        if (count <= 0) {
            return;
        }

        List<Department> departmentList = departments.values().stream()
                .filter(department -> !"ADMIN".equals(department.getCode()))
                .toList();

        List<Position> positionList = positions.values().stream()
                .filter(position -> !"SYS_ADMIN".equals(position.getCode()))
                .toList();

        for (int i = 1; i <= count; i++) {
            Department department = departmentList.get((i - 1) % departmentList.size());
            Position position = positionList.get((i - 1) % positionList.size());
            Role userRole = "HR".equals(department.getCode()) ? hrRole : employeeRole;

            User sampleUser = seedSampleUser(i, userRole);
            seedSampleEmployee(i, department, position, sampleUser);
        }
    }

    private void seedSampleEmployee(int index, Department department, Position position, User user) {
        String number = "%03d".formatted(index);

        Employee employee = employeeRepository.findByEmployeeCode("EMP-" + number).orElseGet(Employee::new);
        employee.setEmployeeCode("EMP-" + number);
        employee.setUser(user);
        employee.setFullName(randomVietnameseFullName(index));
        employee.setDateOfBirth(randomDateOfBirth(index));
        employee.setGender(sampleGender(index));
        employee.setCitizenId(generateCitizenId(index));
        employee.setPhoneNumber(generatePhoneNumber(index));
        employee.setPersonalEmail("nhanvien" + number + "@vantix.vn");
        employee.setAddress(randomVietnameseAddress(index));
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setJoinDate(LocalDate.now().minusDays(10L * index));
        employee.setTerminationDate(sampleStatus(index) == EmploymentStatus.TERMINATED
                ? LocalDate.now().minusDays(index)
                : null);
        employee.setStatus(sampleStatus(index));
        employee.setBankAccount(generateBankAccount(index));
        employee.setTaxCode(generateTaxCode(index));
        employee.setInsuranceNumber(generateInsuranceNumber(index));
        employee.setEmergencyContactName(randomEmergencyContactName(index));
        employee.setEmergencyContactPhone(generateEmergencyPhone(index));

        employeeRepository.save(employee);
    }

    private void seedDepartmentHeads(Map<String, Department> departments) {
        for (Department department : departments.values()) {
            if (department.getHeadEmployeeId() != null) {
                continue;
            }

            employeeRepository.findActiveByDepartmentId(department.getId())
                    .stream()
                    .findFirst()
                    .ifPresent(head -> {
                        department.setHeadEmployeeId(head.getId());
                        departmentRepository.save(department);
                    });
        }
    }

    // ===================== TASK =====================

    private void seedSampleTasks() {
        if (taskRepository.existsByDeletedFalse()) {
            return;
        }

        List<Employee> employees = employeeRepository.findAll()
                .stream()
                .filter(employee -> !Boolean.TRUE.equals(employee.getDeleted()))
                .limit(18)
                .toList();

        if (employees.isEmpty()) {
            return;
        }

        String[] titles = {
                "Chuẩn bị hồ sơ nhân viên mới",
                "Đánh giá kết quả thử việc",
                "Cập nhật sổ tay nhân viên",
                "Thu thập chứng từ tính lương",
                "Lên lịch họp phòng ban",
                "Kiểm tra phân quyền tài khoản",
                "Lập kế hoạch đào tạo nội bộ",
                "Theo dõi yêu cầu cấp thiết bị",
                "Rà soát dữ liệu nhân sự",
                "Chuẩn bị báo cáo nhân sự tháng",
                "Kiểm tra đơn nghỉ phép",
                "Hỗ trợ tuyển dụng ứng viên",
                "Cập nhật danh sách liên hệ khẩn cấp",
                "Xác nhận trưởng phòng ban",
                "Đăng thông báo nội bộ",
                "Soạn ghi chú tuân thủ nội quy",
                "Kiểm tra tiến độ công việc",
                "Cập nhật bảng công việc"
        };

        for (int i = 0; i < titles.length; i++) {
            WorkTask task = new WorkTask();
            task.setTitle(titles[i]);
            task.setDescription("Công việc mẫu số " + "%03d".formatted(i + 1) + " để test giao diện Kanban và danh sách.");
            task.setStatus(sampleTaskStatus(i));
            task.setAssignee(employees.get(i % employees.size()));
            task.setDueDate(LocalDate.now().plusDays(i - 4L));
            taskRepository.save(task);
        }
    }

    private TaskStatus sampleTaskStatus(int index) {
        return switch (index % 3) {
            case 1 -> TaskStatus.DOING;
            case 2 -> TaskStatus.DONE;
            default -> TaskStatus.TODO;
        };
    }

    // ===================== SHIFT =====================

    private Map<String, Shift> seedShifts() {
        Map<String, Shift> shifts = new LinkedHashMap<>();
        shifts.put("FULL", seedShift("FULL", "Ca hành chính", LocalTime.of(8, 0), LocalTime.of(17, 0),
                "Ca làm việc hành chính chuẩn, nghỉ trưa 12:00-13:00"));
        shifts.put("MORNING", seedShift("MORNING", "Ca sáng", LocalTime.of(6, 0), LocalTime.of(12, 0),
                "Ca sáng 6 tiếng"));
        shifts.put("AFTERNOON", seedShift("AFTERNOON", "Ca chiều", LocalTime.of(13, 0), LocalTime.of(19, 0),
                "Ca chiều 6 tiếng"));
        shifts.put("EVENING", seedShift("EVENING", "Ca tối", LocalTime.of(18, 0), LocalTime.of(22, 0),
                "Ca tối 4 tiếng"));
        shifts.put("NIGHT", seedShift("NIGHT", "Ca đêm", LocalTime.of(22, 0), LocalTime.of(6, 0),
                "Ca đêm qua ngày"));
        return shifts;
    }

    private void seedPublicHolidays() {
        int year = LocalDate.now().getYear();
        seedHoliday(LocalDate.of(year, 1, 1), "Tết Dương lịch");
        seedHoliday(LocalDate.of(year, 4, 30), "Ngày Giải phóng miền Nam");
        seedHoliday(LocalDate.of(year, 5, 1), "Ngày Quốc tế Lao động");
        seedHoliday(LocalDate.of(year, 9, 2), "Quốc khánh");
        seedHoliday(LocalDate.of(year, 9, 3), "Nghỉ bù Quốc khánh");
        seedHoliday(LocalDate.of(year, 2, 16), "Tết Nguyên đán");
        seedHoliday(LocalDate.of(year, 2, 17), "Tết Nguyên đán");
        seedHoliday(LocalDate.of(year, 2, 18), "Tết Nguyên đán");
        seedHoliday(LocalDate.of(year, 2, 19), "Tết Nguyên đán");
        seedHoliday(LocalDate.of(year, 2, 20), "Tết Nguyên đán");
        seedHoliday(LocalDate.of(year, 4, 25), "Giỗ Tổ Hùng Vương");
    }

    private void seedHoliday(LocalDate date, String name) {
        if (publicHolidayRepository.existsByHolidayDateAndDeletedFalse(date)) {
            return;
        }
        PublicHoliday holiday = new PublicHoliday();
        holiday.setHolidayDate(date);
        holiday.setName(name);
        holiday.setPaidDay(true);
        publicHolidayRepository.save(holiday);
    }

    private Shift seedShift(String code, String name, LocalTime start, LocalTime end, String description) {
        Shift shift = shiftRepository.findByCode(code).orElseGet(Shift::new);
        shift.setCode(code);
        shift.setName(name);
        shift.setStartTime(start);
        shift.setEndTime(end);
        shift.setDescription(description);
        return shiftRepository.save(shift);
    }

    // ===================== WORK LOCATION =====================

    private Map<String, WorkLocation> seedWorkLocations() {
        Map<String, WorkLocation> locations = new LinkedHashMap<>();
        if (workLocationRepository.existsByDeletedFalse()) {
            workLocationRepository.findAll().stream()
                    .filter(l -> !Boolean.TRUE.equals(l.getDeleted()))
                    .forEach(l -> locations.put(l.getName(), l));
            return locations;
        }

        locations.put("HQ", buildWorkLocation("Trụ sở chính Vantix",
                "Tòa nhà Bitexco, 2 Hải Triều, Bến Nghé, Quận 1, TP.HCM",
                10.771680, 106.704350, 150));
        locations.put("TDC", buildWorkLocation("Văn phòng Thủ Đức",
                "Khu Công nghệ cao, TP. Thủ Đức, TP.HCM",
                10.843870, 106.807020, 200));
        locations.put("HN", buildWorkLocation("Chi nhánh Hà Nội",
                "Keangnam Landmark 72, Phạm Hùng, Nam Từ Liêm, Hà Nội",
                21.017250, 105.784060, 150));
        return locations;
    }

    private WorkLocation buildWorkLocation(String name, String address, double lat, double lng, int radius) {
        WorkLocation location = new WorkLocation();
        location.setName(name);
        location.setAddress(address);
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setRadiusMeters(radius);
        return workLocationRepository.save(location);
    }

    // ===================== WORK SCHEDULE =====================

    private void seedWorkSchedules(Map<String, Shift> shifts, Map<String, WorkLocation> locations) {
        if (shifts.isEmpty() || locations.isEmpty()) {
            return;
        }

        List<Employee> targets = pickScheduledEmployees(scheduleEmployeeCount);
        if (targets.isEmpty()) {
            return;
        }

        Shift defaultShift = shifts.getOrDefault("FULL", shifts.values().iterator().next());
        List<WorkLocation> locationList = new ArrayList<>(locations.values());
        EnumSet<DayOfWeek> workingDays = EnumSet.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
        );

        LocalDate from = LocalDate.now().minusDays(14);
        LocalDate to = LocalDate.now().plusDays(14);

        for (int i = 0; i < targets.size(); i++) {
            Employee employee = targets.get(i);
            WorkLocation location = locationList.get(i % locationList.size());

            LocalDate date = from;
            while (!date.isAfter(to)) {
                if (workingDays.contains(date.getDayOfWeek())
                        && !workScheduleRepository.existsByEmployeeIdAndWorkDateAndDeletedFalse(employee.getId(), date)) {
                    WorkSchedule schedule = new WorkSchedule();
                    schedule.setEmployee(employee);
                    schedule.setShift(defaultShift);
                    schedule.setLocation(location);
                    schedule.setWorkDate(date);
                    schedule.setNote("Lịch mẫu do hệ thống tạo");
                    workScheduleRepository.save(schedule);
                }
                date = date.plusDays(1);
            }
        }
    }

    private List<Employee> pickScheduledEmployees(int limit) {
        return employeeRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
                .filter(e -> e.getStatus() == EmploymentStatus.ACTIVE
                        || e.getStatus() == EmploymentStatus.PROBATION)
                .limit(limit)
                .toList();
    }

    // ===================== ATTENDANCE =====================

    private void seedAttendances() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(14);

        List<WorkSchedule> pastSchedules = workScheduleRepository
                .search(from, today.minusDays(1), null, null);

        for (WorkSchedule schedule : pastSchedules) {
            Employee employee = schedule.getEmployee();
            LocalDate date = schedule.getWorkDate();
            if (attendanceRepository.findByEmployeeAndDate(employee.getId(), date).isPresent()) {
                continue;
            }

            int dice = random.nextInt(100);
            if (dice < 5) {
                // 5% absent
                Attendance absent = new Attendance();
                absent.setEmployee(employee);
                absent.setSchedule(schedule);
                absent.setWorkDate(date);
                absent.setStatus(AttendanceStatus.ABSENT);
                absent.setNote("Không có dữ liệu chấm công");
                attendanceRepository.save(absent);
                continue;
            }

            Attendance attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setSchedule(schedule);
            attendance.setWorkDate(date);

            int lateMinutes = dice < 70 ? 0 : random.nextInt(20) + 5;
            int earlyMinutes = dice < 80 ? 0 : random.nextInt(30) + 5;

            LocalTime shiftStart = schedule.getShift().getStartTime();
            LocalTime shiftEnd = schedule.getShift().getEndTime();

            attendance.setCheckInAt(LocalDateTime.of(date, shiftStart).plusMinutes(lateMinutes));
            attendance.setCheckOutAt(LocalDateTime.of(date, shiftEnd).minusMinutes(earlyMinutes));

            WorkLocation location = schedule.getLocation();
            if (location != null) {
                double[] checkInPoint = jitterPoint(location.getLatitude(), location.getLongitude(), 30);
                double[] checkOutPoint = jitterPoint(location.getLatitude(), location.getLongitude(), 30);
                attendance.setCheckInLat(checkInPoint[0]);
                attendance.setCheckInLng(checkInPoint[1]);
                attendance.setCheckInDistance((double) random.nextInt(40));
                attendance.setCheckOutLat(checkOutPoint[0]);
                attendance.setCheckOutLng(checkOutPoint[1]);
                attendance.setCheckOutDistance((double) random.nextInt(40));
            }

            attendance.setStatus(computeStatus(lateMinutes > 0, earlyMinutes > 0));
            attendanceRepository.save(attendance);
        }
    }

    private AttendanceStatus computeStatus(boolean late, boolean early) {
        if (late && early) return AttendanceStatus.LATE_AND_EARLY;
        if (late) return AttendanceStatus.LATE;
        if (early) return AttendanceStatus.EARLY_LEAVE;
        return AttendanceStatus.ON_TIME;
    }

    private double[] jitterPoint(double lat, double lng, int maxMeters) {
        double dLat = (random.nextDouble() - 0.5) * (maxMeters / 111_000.0) * 2;
        double dLng = (random.nextDouble() - 0.5) * (maxMeters / 111_000.0) * 2;
        return new double[]{lat + dLat, lng + dLng};
    }

    // ===================== LEAVE REQUEST =====================

    private void seedLeaveRequests(User adminUser) {
        if (leaveRequestRepository.count() > 0) {
            return;
        }

        List<Employee> candidates = employeeRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
                .filter(e -> e.getStatus() == EmploymentStatus.ACTIVE)
                .limit(12)
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();

        String[] reasons = {
                "Việc gia đình đột xuất",
                "Khám sức khỏe định kỳ",
                "Đi du lịch cùng gia đình",
                "Chăm sóc con nhỏ bị ốm",
                "Giải quyết thủ tục cá nhân",
                "Về quê dự đám cưới",
                "Nghỉ ngơi phục hồi sau ốm",
                "Thi chứng chỉ chuyên môn"
        };
        LeaveType[] types = LeaveType.values();
        LeaveRequestStatus[] statuses = {
                LeaveRequestStatus.PENDING,
                LeaveRequestStatus.APPROVED,
                LeaveRequestStatus.REJECTED,
                LeaveRequestStatus.CANCELLED
        };

        for (int i = 0; i < candidates.size(); i++) {
            Employee employee = candidates.get(i);
            Employee handover = candidates.get((i + 1) % candidates.size());
            LeaveRequest request = new LeaveRequest();
            request.setEmployee(employee);
            request.setType(types[i % types.length]);

            LocalDate start = today.plusDays(((i % 7) - 3) * 3L);
            int durationDays = (i % 3) + 1;
            request.setStartDate(start);
            request.setEndDate(start.plusDays(durationDays));
            request.setDayUnit(i % 5 == 0 ? LeaveDayUnit.HALF : LeaveDayUnit.FULL);
            request.setReason(reasons[i % reasons.length]);
            request.setHandoverEmployee(handover);
            request.setEmergencyContact("SĐT người thân: 09" + (10000000 + i));

            LeaveRequestStatus status = statuses[i % statuses.length];
            request.setStatus(status);
            if (status != LeaveRequestStatus.PENDING) {
                request.setDecidedBy(adminUser);
                request.setDecidedAt(LocalDateTime.now().minusDays(i % 5L));
                request.setDecisionNote(switch (status) {
                    case APPROVED -> "Đã duyệt theo quy định công ty";
                    case REJECTED -> "Không đủ số ngày phép còn lại";
                    case CANCELLED -> "Nhân viên tự hủy đơn";
                    default -> null;
                });
            }

            leaveRequestRepository.save(request);
        }
    }

    // ===================== CONTRACT =====================

    private void seedContracts() {
        List<Employee> employees = employeeRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
                .filter(e -> e.getStatus() == EmploymentStatus.ACTIVE
                        || e.getStatus() == EmploymentStatus.PROBATION)
                .sorted(Comparator.comparing(Employee::getEmployeeCode))
                .limit(45)
                .toList();

        LocalDate today = LocalDate.now();

        for (int i = 0; i < employees.size(); i++) {
            Employee employee = employees.get(i);
            String code = "HD-%s-001".formatted(employee.getEmployeeCode().replace("EMP-", ""));
            if (contractRepository.existsByContractCodeAndDeletedFalse(code)) {
                continue;
            }

            Contract contract = new Contract();
            contract.setContractCode(code);
            contract.setEmployee(employee);
            contract.setPosition(employee.getPosition());

            boolean probation = employee.getStatus() == EmploymentStatus.PROBATION || i % 11 == 0;
            boolean indefinite = i % 5 == 0;
            boolean draft = i % 13 == 0;
            boolean expiringSoon = i % 9 == 0;

            if (probation) {
                contract.setContractType(ContractType.PROBATION);
                contract.setProbationMonths(2);
                contract.setNoticePeriodDays(3);
            } else if (indefinite) {
                contract.setContractType(ContractType.INDEFINITE);
                contract.setNoticePeriodDays(45);
            } else {
                contract.setContractType(ContractType.FIXED_TERM);
                contract.setNoticePeriodDays(30);
            }

            LocalDate startDate = employee.getJoinDate() != null
                    ? employee.getJoinDate()
                    : today.minusMonths(12 + i % 18L);
            if (startDate.isAfter(today.minusDays(10))) {
                startDate = today.minusMonths(2).minusDays(i % 20L);
            }

            contract.setSignedDate(startDate.minusDays(5));
            contract.setStartDate(startDate);
            if (contract.getContractType() == ContractType.INDEFINITE) {
                contract.setEndDate(null);
            } else if (expiringSoon) {
                contract.setEndDate(today.plusDays(7 + i % 20L));
            } else if (probation) {
                contract.setEndDate(startDate.plusMonths(2));
            } else {
                contract.setEndDate(startDate.plusMonths(24));
            }

            contract.setStatus(draft ? ContractStatus.DRAFT : ContractStatus.ACTIVE);
            contract.setBaseSalary(sampleBaseSalary(i, probation));
            contract.setInsuranceSalary(contract.getBaseSalary().min(new BigDecimal("46800000")));
            contract.setResponsibilityAllowance(new BigDecimal((i % 4) * 500000L));
            contract.setMealAllowance(new BigDecimal("730000"));
            contract.setTransportAllowance(new BigDecimal(300000 + (i % 3) * 100000L));
            contract.setPhoneAllowance(new BigDecimal(200000 + (i % 4) * 50000L));
            contract.setOtherAllowance(new BigDecimal((i % 5) * 200000L));
            contract.setStandardWorkDays(26);
            contract.setHoursPerDay(new BigDecimal("8.00"));
            contract.setNote("Hợp đồng mẫu phục vụ demo quản lý hợp đồng và tính lương.");

            contractRepository.save(contract);

            if (i < 8) {
                seedHistoricalContract(employee, i, startDate);
            }
        }
    }

    private void seedHistoricalContract(Employee employee, int index, LocalDate activeStartDate) {
        String code = "HD-%s-000".formatted(employee.getEmployeeCode().replace("EMP-", ""));
        if (contractRepository.existsByContractCodeAndDeletedFalse(code)) {
            return;
        }

        Contract oldContract = new Contract();
        oldContract.setContractCode(code);
        oldContract.setEmployee(employee);
        oldContract.setPosition(employee.getPosition());
        oldContract.setContractType(ContractType.FIXED_TERM);
        oldContract.setStatus(index % 2 == 0 ? ContractStatus.EXPIRED : ContractStatus.TERMINATED);
        oldContract.setSignedDate(activeStartDate.minusMonths(13).minusDays(5));
        oldContract.setStartDate(activeStartDate.minusMonths(13));
        oldContract.setEndDate(activeStartDate.minusDays(1));
        oldContract.setBaseSalary(sampleBaseSalary(index, false).subtract(new BigDecimal("1500000")).max(new BigDecimal("6000000")));
        oldContract.setInsuranceSalary(oldContract.getBaseSalary());
        oldContract.setMealAllowance(new BigDecimal("730000"));
        oldContract.setTransportAllowance(new BigDecimal("300000"));
        oldContract.setPhoneAllowance(new BigDecimal("200000"));
        oldContract.setStandardWorkDays(26);
        oldContract.setHoursPerDay(new BigDecimal("8.00"));
        oldContract.setNoticePeriodDays(30);
        if (oldContract.getStatus() == ContractStatus.TERMINATED) {
            oldContract.setTerminatedDate(activeStartDate.minusDays(1));
            oldContract.setTerminationReason("Chuyển sang hợp đồng mới.");
        }
        oldContract.setNote("Hợp đồng lịch sử mẫu.");
        contractRepository.save(oldContract);
    }

    private BigDecimal sampleBaseSalary(int index, boolean probation) {
        BigDecimal salary = new BigDecimal(9000000 + (index % 12) * 1500000L);
        if (index % 10 == 0) {
            salary = salary.add(new BigDecimal("12000000"));
        }
        return probation ? salary.multiply(new BigDecimal("0.85")).setScale(0) : salary;
    }

    // ===================== PAYROLL =====================

    private void seedPayrollSamples(User adminUser) {
        YearMonth current = YearMonth.now();
        PayrollPeriod currentPeriod = seedPayrollPeriod(current, PayrollStatus.CALCULATED, null);
        PayrollPeriod previousPeriod = seedPayrollPeriod(current.minusMonths(1), PayrollStatus.APPROVED, adminUser);

        seedPayrollRows(currentPeriod, PayrollStatus.CALCULATED, current);
        seedPayrollRows(previousPeriod, PayrollStatus.APPROVED, current.minusMonths(1));
    }

    private PayrollPeriod seedPayrollPeriod(YearMonth yearMonth, PayrollStatus status, User approvedBy) {
        return payrollPeriodRepository.findByYearAndMonth(yearMonth.getYear(), yearMonth.getMonthValue())
                .map(period -> {
                    period.setDeleted(false);
                    period.setDeletedAt(null);
                    period.setDeletedBy(null);
                    if (period.getStartDate() == null) {
                        period.setStartDate(yearMonth.atDay(1));
                    }
                    if (period.getEndDate() == null) {
                        period.setEndDate(yearMonth.atEndOfMonth());
                    }
                    if (period.getStandardWorkDays() == null) {
                        period.setStandardWorkDays(26);
                    }
                    if (period.getStatus() == null || period.getStatus() == PayrollStatus.CANCELLED) {
                        period.setStatus(status);
                    }
                    if (approvedBy != null && period.getApprovedBy() == null) {
                        period.setApprovedBy(approvedBy);
                        period.setApprovedAt(LocalDateTime.now().minusDays(3));
                    }
                    return payrollPeriodRepository.save(period);
                })
                .orElseGet(() -> {
                    PayrollPeriod period = new PayrollPeriod();
                    period.setYear(yearMonth.getYear());
                    period.setMonth(yearMonth.getMonthValue());
                    period.setStartDate(yearMonth.atDay(1));
                    period.setEndDate(yearMonth.atEndOfMonth());
                    period.setStandardWorkDays(26);
                    period.setStatus(status);
                    period.setNote("Kỳ lương mẫu do hệ thống tạo.");
                    if (approvedBy != null) {
                        period.setApprovedBy(approvedBy);
                        period.setApprovedAt(LocalDateTime.now().minusDays(3));
                    }
                    return payrollPeriodRepository.save(period);
                });
    }

    private void seedPayrollRows(PayrollPeriod period, PayrollStatus status, YearMonth yearMonth) {
        List<Employee> employees = employeeRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
                .filter(e -> e.getStatus() == EmploymentStatus.ACTIVE
                        || e.getStatus() == EmploymentStatus.PROBATION)
                .sorted(Comparator.comparing(Employee::getEmployeeCode))
                .limit(30)
                .toList();

        for (int i = 0; i < employees.size(); i++) {
            Employee employee = employees.get(i);
            if (payrollRepository.findByPeriodIdAndEmployeeIdAndDeletedFalse(period.getId(), employee.getId()).isPresent()) {
                continue;
            }

            Contract contract = contractRepository
                    .findEffectiveContracts(employee.getId(), period.getEndDate())
                    .stream()
                    .findFirst()
                    .orElse(null);

            Payroll payroll = new Payroll();
            payroll.setPeriod(period);
            payroll.setEmployee(employee);
            payroll.setContract(contract);
            payroll.setStandardWorkDays(contract != null && contract.getStandardWorkDays() != null
                    ? contract.getStandardWorkDays()
                    : period.getStandardWorkDays());
            payroll.setBaseSalary(contract != null ? nz(contract.getBaseSalary()) : sampleBaseSalary(i, false));
            payroll.setInsuranceSalary(contract != null && contract.getInsuranceSalary() != null
                    ? contract.getInsuranceSalary()
                    : payroll.getBaseSalary());
            payroll.setActualWorkDays(sampleActualWorkDays(employee, period, yearMonth, i));
            payroll.setPaidLeaveDays(new BigDecimal(i % 6 == 0 ? "1.00" : "0.00"));
            payroll.setUnpaidLeaveDays(new BigDecimal(i % 17 == 0 ? "0.50" : "0.00"));
            payroll.setOvertimeHoursWeekday(new BigDecimal((i % 5) * 2L));
            payroll.setOvertimeHoursWeekend(new BigDecimal(i % 7 == 0 ? "4.00" : "0.00"));
            payroll.setOvertimeHoursHoliday(BigDecimal.ZERO);
            payroll.setOvertimeHoursNight(new BigDecimal(i % 9 == 0 ? "3.00" : "0.00"));
            payroll.setDependents(i % 4);
            payroll.setResponsibilityAllowance(contract != null ? nz(contract.getResponsibilityAllowance()) : BigDecimal.ZERO);
            payroll.setMealAllowance(contract != null ? nz(contract.getMealAllowance()) : new BigDecimal("730000"));
            payroll.setTransportAllowance(contract != null ? nz(contract.getTransportAllowance()) : new BigDecimal("300000"));
            payroll.setPhoneAllowance(contract != null ? nz(contract.getPhoneAllowance()) : new BigDecimal("200000"));
            payroll.setOtherAllowance(contract != null ? nz(contract.getOtherAllowance()) : BigDecimal.ZERO);
            payroll.setBonus(new BigDecimal(i % 8 == 0 ? "1500000" : "0"));
            payroll.setCommission(new BigDecimal(i % 10 == 0 ? "2500000" : "0"));
            payroll.setOtherDeductions(new BigDecimal(i % 12 == 0 ? "300000" : "0"));
            payroll.setNote("Dòng lương mẫu có thể điều chỉnh để demo tính lại.");
            payroll.setStatus(status);

            applyPayrollCalculation(payroll);
            payrollRepository.save(payroll);
        }
    }

    private BigDecimal sampleActualWorkDays(Employee employee, PayrollPeriod period, YearMonth yearMonth, int index) {
        List<Attendance> attendances = attendanceRepository.search(period.getStartDate(), period.getEndDate(), employee.getId());
        long workedDays = attendances.stream()
                .filter(a -> a.getStatus() != AttendanceStatus.ABSENT
                        && a.getStatus() != AttendanceStatus.PENDING
                        && a.getCheckInAt() != null)
                .count();

        if (workedDays > 0) {
            return new BigDecimal(workedDays);
        }

        boolean currentMonth = yearMonth.equals(YearMonth.now());
        int fallback = currentMonth ? 12 + (index % 8) : 22 + (index % 4);
        return new BigDecimal(Math.min(fallback, period.getStandardWorkDays()));
    }

    private void applyPayrollCalculation(Payroll payroll) {
        PayrollCalculation result = VietnamPayrollCalculator.calculate(PayrollInput.builder()
                .baseSalary(payroll.getBaseSalary())
                .insuranceSalary(payroll.getInsuranceSalary())
                .standardWorkDays(payroll.getStandardWorkDays() != null ? payroll.getStandardWorkDays() : 26)
                .hoursPerDay(payroll.getContract() != null ? payroll.getContract().getHoursPerDay() : new BigDecimal("8.00"))
                .actualWorkDays(payroll.getActualWorkDays())
                .paidLeaveDays(payroll.getPaidLeaveDays())
                .overtimeHoursWeekday(payroll.getOvertimeHoursWeekday())
                .overtimeHoursWeekend(payroll.getOvertimeHoursWeekend())
                .overtimeHoursHoliday(payroll.getOvertimeHoursHoliday())
                .overtimeHoursNight(payroll.getOvertimeHoursNight())
                .responsibilityAllowance(payroll.getResponsibilityAllowance())
                .mealAllowance(payroll.getMealAllowance())
                .transportAllowance(payroll.getTransportAllowance())
                .phoneAllowance(payroll.getPhoneAllowance())
                .otherAllowance(payroll.getOtherAllowance())
                .bonus(payroll.getBonus())
                .commission(payroll.getCommission())
                .otherDeductions(payroll.getOtherDeductions())
                .dependents(payroll.getDependents())
                .build());

        payroll.setWorkingDaysSalary(result.getWorkingDaysSalary());
        payroll.setOvertimePay(result.getOvertimePay());
        payroll.setTotalAllowance(result.getAllowance());
        payroll.setGrossIncome(result.getGrossIncome());
        payroll.setSocialInsurance(result.getSocialInsurance());
        payroll.setHealthInsurance(result.getHealthInsurance());
        payroll.setUnemploymentInsurance(result.getUnemploymentInsurance());
        payroll.setTotalEmployeeInsurance(result.getTotalEmployeeInsurance());
        payroll.setTaxableIncome(result.getTaxableIncome());
        payroll.setPersonalIncomeTax(result.getPersonalIncomeTax());
        payroll.setNetIncome(result.getNetIncome());
        payroll.setEmployerInsurance(result.getEmployerInsurance());
        payroll.setTotalEmployerCost(result.getTotalEmployerCost());
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    // ===================== NOTIFICATION =====================

    private void seedNotifications(User adminUser) {
        if (notificationRepository.count() > 0) {
            return;
        }

        record NotifSample(NotificationType type, String title, String message, String target) {}

        List<NotifSample> samples = List.of(
                new NotifSample(NotificationType.SYSTEM,
                        "Chào mừng đến với Vantix HR",
                        "Hệ thống đã sẵn sàng với dữ liệu mẫu. Đăng nhập bằng admin/Admin@123.",
                        "/"),
                new NotifSample(NotificationType.LEAVE,
                        "Có đơn xin nghỉ chờ duyệt",
                        "Một số nhân viên đã gửi đơn xin nghỉ, vui lòng xem xét.",
                        "/leave-requests"),
                new NotifSample(NotificationType.ATTENDANCE,
                        "Cập nhật chấm công",
                        "Dữ liệu chấm công 2 tuần gần nhất đã được khởi tạo.",
                        "/attendance"),
                new NotifSample(NotificationType.TASK,
                        "Công việc mới được giao",
                        "Các công việc mẫu đã được tạo trên bảng Kanban.",
                        "/tasks"),
                new NotifSample(NotificationType.PAYROLL,
                        "Bảng lương tháng này",
                        "Bảng lương tháng hiện tại đang được chuẩn bị.",
                        "/dashboard")
        );

        for (int i = 0; i < samples.size(); i++) {
            NotifSample sample = samples.get(i);
            Notification notification = new Notification();
            notification.setUser(adminUser);
            notification.setType(sample.type());
            notification.setTitle(sample.title());
            notification.setMessage(sample.message());
            notification.setTargetUrl(sample.target());
            notification.setStatus(i < 2 ? NotificationStatus.UNREAD : NotificationStatus.READ);
            if (notification.getStatus() == NotificationStatus.READ) {
                notification.setReadAt(LocalDateTime.now().minusHours(i));
            }
            notificationRepository.save(notification);
        }
    }

    // ===================== SAMPLE DATA =====================

    private Gender sampleGender(int index) {
        if (index % 11 == 0) {
            return Gender.OTHER;
        }
        return index % 2 == 0 ? Gender.MALE : Gender.FEMALE;
    }

    private EmploymentStatus sampleStatus(int index) {
        if (index % 29 == 0) {
            return EmploymentStatus.TERMINATED;
        }
        if (index % 19 == 0) {
            return EmploymentStatus.RESIGNED;
        }
        if (index % 13 == 0) {
            return EmploymentStatus.UNPAID_LEAVE;
        }
        if (index % 7 == 0) {
            return EmploymentStatus.PROBATION;
        }
        return EmploymentStatus.ACTIVE;
    }

    private LocalDate randomDateOfBirth(int index) {
        int year = 1988 + (index % 12);
        int month = (index % 12) + 1;
        int day = (index % 28) + 1;
        return LocalDate.of(year, month, day);
    }

    private String randomVietnameseFullName(int index) {
        String[] ho = {
                "Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ",
                "Võ", "Đặng", "Bùi", "Đỗ", "Hồ", "Ngô", "Dương"
        };

        String[] tenDemNam = {
                "Văn", "Hữu", "Đức", "Minh", "Quang", "Thanh", "Công", "Xuân", "Gia", "Anh"
        };

        String[] tenDemNu = {
                "Thị", "Ngọc", "Thanh", "Thuỳ", "Diễm", "Kim", "Bảo", "Mỹ", "Phương", "Mai"
        };

        String[] tenNam = {
                "An", "Bình", "Cường", "Dũng", "Hải", "Hưng", "Khánh", "Long", "Nam", "Phong",
                "Quân", "Sơn", "Tài", "Tuấn", "Vinh", "Đạt"
        };

        String[] tenNu = {
                "Anh", "Chi", "Dung", "Giang", "Hạnh", "Hoa", "Lan", "Linh", "My", "Nga",
                "Nhung", "Phương", "Thảo", "Trang", "Vy", "Yến"
        };

        Gender gender = sampleGender(index);
        String lastName = ho[random.nextInt(ho.length)];

        if (gender == Gender.MALE) {
            return lastName + " "
                    + tenDemNam[random.nextInt(tenDemNam.length)] + " "
                    + tenNam[random.nextInt(tenNam.length)];
        }

        if (gender == Gender.FEMALE) {
            return lastName + " "
                    + tenDemNu[random.nextInt(tenDemNu.length)] + " "
                    + tenNu[random.nextInt(tenNu.length)];
        }

        String[] neutralMiddle = {"Gia", "Minh", "Thanh", "Bảo", "Anh"};
        String[] neutralFirst = {"An", "Khôi", "Nhi", "Vy", "Phúc", "Lâm", "Trân", "Khoa"};
        return lastName + " "
                + neutralMiddle[random.nextInt(neutralMiddle.length)] + " "
                + neutralFirst[random.nextInt(neutralFirst.length)];
    }

    private String randomVietnameseAddress(int index) {
        String[] streets = {
                "Nguyễn Huệ", "Lê Lợi", "Trần Hưng Đạo", "Điện Biên Phủ", "Cách Mạng Tháng 8",
                "Phan Xích Long", "Nguyễn Thị Minh Khai", "Hoàng Văn Thụ", "Lý Thường Kiệt", "Quang Trung"
        };

        String[] districts = {
                "Quận 1", "Quận 3", "Quận 5", "Quận 7", "Quận 10",
                "Quận Bình Thạnh", "Quận Tân Bình", "Quận Gò Vấp", "TP Thủ Đức", "Quận Phú Nhuận"
        };

        return "Số " + (10 + index) + " đường "
                + streets[random.nextInt(streets.length)] + ", "
                + districts[random.nextInt(districts.length)] + ", TP.HCM";
    }

    private String randomEmergencyContactName(int index) {
        String[] relations = {"Cha", "Mẹ", "Anh", "Chị", "Em", "Vợ", "Chồng"};
        return relations[index % relations.length] + " - " + randomVietnameseShortName();
    }

    private String randomVietnameseShortName() {
        String[] ho = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Võ", "Đặng"};
        String[] ten = {"An", "Bình", "Dũng", "Hạnh", "Lan", "Linh", "Phương", "Trang", "Tuấn", "Vy"};
        return ho[random.nextInt(ho.length)] + " " + ten[random.nextInt(ten.length)];
    }

    private String generateCitizenId(int index) {
        return "0792%08d".formatted(index);
    }

    private String generatePhoneNumber(int index) {
        return "09%08d".formatted(index % 100000000);
    }

    private String generateEmergencyPhone(int index) {
        return "08%08d".formatted((index * 7) % 100000000);
    }

    private String generateBankAccount(int index) {
        return "9704%012d".formatted(index);
    }

    private String generateTaxCode(int index) {
        return "MST%08d".formatted(index);
    }

    private String generateInsuranceNumber(int index) {
        return "BHXH%08d".formatted(index);
    }
}
