package poly.edu.vantix_hrm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.entity.*;
import poly.edu.vantix_hrm.entity.Contract.ContractStatus;
import poly.edu.vantix_hrm.entity.Contract.Type;
import poly.edu.vantix_hrm.entity.Employee.Gender;
import poly.edu.vantix_hrm.entity.Employee.WorkStatus;
import poly.edu.vantix_hrm.entity.MonthlySchedules.ScheduleStatus;
import poly.edu.vantix_hrm.entity.User.UserStatus;
import poly.edu.vantix_hrm.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/*
 * DataSeeder
 * -------------------------------------------
 * Tự động khởi tạo dữ liệu mẫu khi ứng dụng khởi động.
 * Chỉ chạy một lần duy nhất — nếu đã có permission trong DB thì bỏ qua.
 *
 * Thứ tự seed:
 *   1. Permissions  → 2. Roles  → 3. Departments  → 4. Positions
 *   → 5. Users  → 6. Employees  → 7. Department managers
 *   → 8. Shifts  → 9. Leave Types  → 10. Monthly Schedules  → 11. Contracts
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final PermissionRepository   permissionRepository;
    private final RoleRepository         roleRepository;
    private final DepartmentRepository   departmentRepository;
    private final PositionRepository     positionRepository;
    private final UserRepository         userRepository;
    private final EmployeeRepository     employeeRepository;
    private final ShiftsRepository       shiftsRepository;
    private final LeaveTypeRepository    leaveTypeRepository;
    private final MonthlySchedulesRepository monthlySchedulesRepository;
    private final ContractsRepository    contractsRepository;
    private final PasswordEncoder        passwordEncoder;

    private static final String DEFAULT_PASSWORD = "123456";

    // ─── Entry point ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (permissionRepository.count() > 0) {
            log.info("[DataSeeder] Dữ liệu đã tồn tại, bỏ qua seed.");
            return;
        }

        log.info("[DataSeeder] Bắt đầu khởi tạo dữ liệu mẫu...");

        Map<String, Permission>  perms     = seedPermissions();
        Map<String, Role>        roles     = seedRoles(perms);
        Map<String, Department>  depts     = seedDepartments();
        Map<String, Position>    positions = seedPositions(depts);
        Map<String, User>        users     = seedUsers(roles);
        Map<String, Employee>    employees = seedEmployees(users, depts, positions);
        updateDepartmentManagers(depts, employees);
        seedShifts();
        seedLeaveTypes();
        seedMonthlySchedules(employees);
        seedContracts(employees, positions);

        log.info("[DataSeeder] Hoàn tất! Tài khoản mặc định — password: {}", DEFAULT_PASSWORD);
    }

    // ─── 1. Permissions ───────────────────────────────────────────────────────

    private Map<String, Permission> seedPermissions() {
        List<Object[]> defs = List.of(
                // User
                new Object[]{"USER_VIEW",                    "Xem thông tin người dùng"},
                new Object[]{"USER_CREATE",                  "Tạo người dùng mới"},
                new Object[]{"USER_UPDATE",                  "Cập nhật thông tin người dùng"},
                new Object[]{"USER_DELETE",                  "Xóa người dùng"},
                new Object[]{"USER_STATUS_UPDATE",           "Thay đổi trạng thái tài khoản"},
                // Permission
                new Object[]{"PERMISSION_VIEW",              "Xem danh sách quyền"},
                new Object[]{"PERMISSION_CREATE",            "Tạo quyền mới"},
                new Object[]{"PERMISSION_UPDATE",            "Cập nhật quyền"},
                new Object[]{"PERMISSION_DELETE",            "Xóa quyền"},
                // Role
                new Object[]{"ROLE_VIEW",                    "Xem danh sách vai trò"},
                new Object[]{"ROLE_CREATE",                  "Tạo vai trò mới"},
                new Object[]{"ROLE_UPDATE",                  "Cập nhật vai trò"},
                new Object[]{"ROLE_DELETE",                  "Xóa vai trò"},
                // Employee
                new Object[]{"EMPLOYEE_VIEW",                "Xem thông tin nhân viên"},
                new Object[]{"EMPLOYEE_CREATE",              "Tạo nhân viên mới"},
                new Object[]{"EMPLOYEE_UPDATE",              "Cập nhật thông tin nhân viên"},
                new Object[]{"EMPLOYEE_DELETE",              "Xóa nhân viên"},
                // Department
                new Object[]{"DEPARTMENT_VIEW",              "Xem thông tin phòng ban"},
                new Object[]{"DEPARTMENT_CREATE",            "Tạo phòng ban mới"},
                new Object[]{"DEPARTMENT_UPDATE",            "Cập nhật phòng ban"},
                new Object[]{"DEPARTMENT_DELETE",            "Xóa phòng ban"},
                // Position
                new Object[]{"POSITION_VIEW",                "Xem thông tin vị trí"},
                new Object[]{"POSITION_CREATE",              "Tạo vị trí mới"},
                new Object[]{"POSITION_UPDATE",              "Cập nhật vị trí"},
                new Object[]{"POSITION_DELETE",              "Xóa vị trí"},
                // Attendance
                new Object[]{"ATTENDANCE_VIEW",              "Xem bảng chấm công"},
                new Object[]{"ATTENDANCE_CHECKIN",           "Chấm công vào ca"},
                new Object[]{"ATTENDANCE_CHECKOUT",          "Chấm công ra ca"},
                // Attendance management
                new Object[]{"ATTENDANCE_MANAGEMENT_VIEW",   "Xem danh sách chấm công cần duyệt"},
                new Object[]{"ATTENDANCE_MANAGEMENT_APPROVE","Duyệt/Từ chối chấm công"},
                // Leave type
                new Object[]{"LEAVE_TYPE_VIEW",              "Xem danh sách loại nghỉ phép"},
                new Object[]{"LEAVE_TYPE_CREATE",            "Tạo loại nghỉ phép mới"},
                new Object[]{"LEAVE_TYPE_UPDATE",            "Cập nhật loại nghỉ phép"},
                new Object[]{"LEAVE_TYPE_DELETE",            "Xóa loại nghỉ phép"},
                // Leave request
                new Object[]{"LEAVE_VIEW",                   "Xem đơn xin nghỉ của bản thân"},
                new Object[]{"LEAVE_CREATE",                 "Nộp đơn xin nghỉ"},
                new Object[]{"LEAVE_MANAGE",                 "Duyệt/Từ chối đơn xin nghỉ"},
                // Contract
                new Object[]{"CONTRACT_VIEW",                "Xem danh sách hợp đồng"},
                new Object[]{"CONTRACT_CREATE",              "Tạo hợp đồng mới"},
                new Object[]{"CONTRACT_DELETE",              "Xóa hợp đồng"},
                new Object[]{"CONTRACT_STATUS_UPDATE",       "Cập nhật trạng thái hợp đồng"},
                // Contract annex
                new Object[]{"CONTRACT_ANNEX_VIEW",          "Xem phụ lục hợp đồng"},
                new Object[]{"CONTRACT_ANNEX_CREATE",        "Tạo phụ lục hợp đồng mới"},
                new Object[]{"CONTRACT_ANNEX_UPDATE",        "Cập nhật phụ lục hợp đồng"},
                new Object[]{"CONTRACT_ANNEX_DELETE",        "Xóa phụ lục hợp đồng"},
                // Schedule
                new Object[]{"SCHEDULE_VIEW",                "Xem lịch làm việc"},
                new Object[]{"SCHEDULE_CREATE",              "Phân ca làm việc"},
                new Object[]{"SCHEDULE_STATUS_UPDATE",       "Cập nhật trạng thái lịch làm việc"},
                // Other
                new Object[]{"SALARY_VIEW",                  "Xem bảng lương"},
                new Object[]{"REPORT_VIEW",                  "Xem báo cáo"},
                new Object[]{"SYSTEM_CONFIG",                "Cấu hình hệ thống"}
        );

        Map<String, Permission> result = new LinkedHashMap<>();
        for (Object[] def : defs) {
            Permission p = Permission.builder()
                    .name((String) def[0])
                    .description((String) def[1])
                    .build();
            result.put(p.getName(), permissionRepository.save(p));
        }
        log.info("[DataSeeder] ✓ {} permissions", result.size());
        return result;
    }

    // ─── 2. Roles ─────────────────────────────────────────────────────────────

    private Map<String, Role> seedRoles(Map<String, Permission> perms) {
        // ADMIN: tất cả quyền
        Role admin = buildRole("ADMIN", "Quản trị viên hệ thống - có tất cả quyền",
                new HashSet<>(perms.values()));

        // HR_MANAGER
        Role hrManager = buildRole("HR_MANAGER", "Quản lý nhân sự", permissions(perms,
                "USER_VIEW", "USER_CREATE", "USER_UPDATE", "USER_DELETE", "USER_STATUS_UPDATE",
                "EMPLOYEE_VIEW", "EMPLOYEE_CREATE", "EMPLOYEE_UPDATE", "EMPLOYEE_DELETE",
                "DEPARTMENT_VIEW", "DEPARTMENT_CREATE", "DEPARTMENT_UPDATE", "DEPARTMENT_DELETE",
                "POSITION_VIEW", "POSITION_CREATE", "POSITION_UPDATE", "POSITION_DELETE",
                "ATTENDANCE_VIEW", "ATTENDANCE_MANAGEMENT_VIEW", "ATTENDANCE_MANAGEMENT_APPROVE",
                "LEAVE_TYPE_VIEW", "LEAVE_TYPE_CREATE", "LEAVE_TYPE_UPDATE", "LEAVE_TYPE_DELETE",
                "LEAVE_VIEW", "LEAVE_MANAGE",
                "CONTRACT_VIEW", "CONTRACT_CREATE", "CONTRACT_DELETE", "CONTRACT_STATUS_UPDATE",
                "CONTRACT_ANNEX_VIEW", "CONTRACT_ANNEX_CREATE", "CONTRACT_ANNEX_UPDATE", "CONTRACT_ANNEX_DELETE",
                "SCHEDULE_VIEW", "SCHEDULE_CREATE", "SCHEDULE_STATUS_UPDATE",
                "SALARY_VIEW", "REPORT_VIEW"
        ));

        // DEPARTMENT_MANAGER
        Role deptManager = buildRole("DEPARTMENT_MANAGER", "Trưởng phòng - quản lý nhân viên trong phòng",
                permissions(perms,
                        "EMPLOYEE_VIEW", "EMPLOYEE_UPDATE",
                        "DEPARTMENT_VIEW", "POSITION_VIEW",
                        "ATTENDANCE_VIEW", "ATTENDANCE_MANAGEMENT_VIEW", "ATTENDANCE_MANAGEMENT_APPROVE",
                        "LEAVE_TYPE_VIEW", "LEAVE_VIEW", "LEAVE_MANAGE",
                        "CONTRACT_VIEW", "CONTRACT_ANNEX_VIEW",
                        "SCHEDULE_VIEW", "SCHEDULE_CREATE", "SCHEDULE_STATUS_UPDATE"
                ));

        // EMPLOYEE
        Role employee = buildRole("EMPLOYEE", "Nhân viên - quyền cơ bản", permissions(perms,
                "EMPLOYEE_VIEW", "DEPARTMENT_VIEW", "POSITION_VIEW",
                "ATTENDANCE_VIEW", "ATTENDANCE_CHECKIN", "ATTENDANCE_CHECKOUT",
                "LEAVE_TYPE_VIEW", "LEAVE_VIEW", "LEAVE_CREATE",
                "SCHEDULE_VIEW"
        ));

        Map<String, Role> result = new LinkedHashMap<>();
        result.put("ADMIN",              roleRepository.save(admin));
        result.put("HR_MANAGER",         roleRepository.save(hrManager));
        result.put("DEPARTMENT_MANAGER", roleRepository.save(deptManager));
        result.put("EMPLOYEE",           roleRepository.save(employee));

        log.info("[DataSeeder] ✓ {} roles", result.size());
        return result;
    }

    private Role buildRole(String name, String description, Set<Permission> permissions) {
        return Role.builder().name(name).description(description).permissions(permissions).build();
    }

    private Set<Permission> permissions(Map<String, Permission> perms, String... names) {
        Set<Permission> set = new HashSet<>();
        for (String name : names) set.add(perms.get(name));
        return set;
    }

    // ─── 3. Departments ───────────────────────────────────────────────────────

    private Map<String, Department> seedDepartments() {
        Map<String, Department> result = new LinkedHashMap<>();

        List<Object[]> defs = List.of(
                new Object[]{"BGD",        "Ban Giám Đốc",       "Quản lý điều hành công ty"},
                new Object[]{"HR",         "Phòng Nhân Sự",      "Quản lý nhân sự, tuyển dụng, đào tạo"},
                new Object[]{"KETOAN",     "Phòng Kế Toán",      "Quản lý tài chính, kế toán, lương thưởng"},
                new Object[]{"KINHDOANH",  "Phòng Kinh Doanh",   "Phát triển thị trường, bán hàng"},
                new Object[]{"IT",         "Phòng IT",           "Quản trị hệ thống, phát triển phần mềm"},
                new Object[]{"MARKETING",  "Phòng Marketing",    "Quảng bá thương hiệu, tiếp thị sản phẩm"}
        );

        for (Object[] def : defs) {
            Department dept = Department.builder()
                    .name((String) def[1])
                    .description((String) def[2])
                    .build();
            result.put((String) def[0], departmentRepository.save(dept));
        }
        log.info("[DataSeeder] ✓ {} departments", result.size());
        return result;
    }

    // ─── 4. Positions ─────────────────────────────────────────────────────────

    private Map<String, Position> seedPositions(Map<String, Department> depts) {
        Map<String, Position> result = new LinkedHashMap<>();

        // Vị trí dùng chung cho tất cả trưởng phòng (id thấp nhất - kiểm tra isManager)
        result.put("TRUONG_PHONG", save(positionRepository, "Trưởng Phòng", "Trưởng phòng ban", depts.get("BGD")));
        result.put("PHO_GD",       save(positionRepository, "Phó Giám Đốc", "Hỗ trợ Giám đốc điều hành", depts.get("BGD")));

        // Phòng Nhân Sự
        result.put("TP_HR",        save(positionRepository, "Trưởng Phòng Nhân Sự",  "Quản lý toàn bộ hoạt động nhân sự", depts.get("HR")));
        result.put("CV_HR",        save(positionRepository, "Chuyên Viên Nhân Sự",    "Thực hiện nghiệp vụ nhân sự",       depts.get("HR")));
        result.put("NV_TUYENDUNG", save(positionRepository, "Nhân Viên Tuyển Dụng",   "Phụ trách tuyển dụng",              depts.get("HR")));

        // Phòng Kế Toán
        result.put("TP_KT",        save(positionRepository, "Trưởng Phòng Kế Toán",  "Quản lý tài chính kế toán",         depts.get("KETOAN")));
        result.put("KE_TOAN_TRUONG", save(positionRepository, "Kế Toán Trưởng",      "Phụ trách kế toán tổng hợp",        depts.get("KETOAN")));
        result.put("KE_TOAN_VIEN", save(positionRepository, "Kế Toán Viên",          "Thực hiện công việc kế toán",        depts.get("KETOAN")));

        // Phòng Kinh Doanh
        result.put("TP_KD",        save(positionRepository, "Trưởng Phòng Kinh Doanh", "Quản lý hoạt động kinh doanh",    depts.get("KINHDOANH")));
        result.put("CV_KD",        save(positionRepository, "Chuyên Viên Kinh Doanh",  "Phát triển khách hàng",            depts.get("KINHDOANH")));
        result.put("NV_BANHANG",   save(positionRepository, "Nhân Viên Bán Hàng",      "Tư vấn và bán hàng",              depts.get("KINHDOANH")));

        // Phòng IT
        result.put("TP_IT",        save(positionRepository, "Trưởng Phòng IT",   "Quản lý hệ thống và đội ngũ IT",  depts.get("IT")));
        result.put("DEVELOPER",    save(positionRepository, "Developer",          "Phát triển phần mềm",              depts.get("IT")));
        result.put("SYS_ADMIN",    save(positionRepository, "System Admin",       "Quản trị hệ thống",                depts.get("IT")));
        result.put("DBA",          save(positionRepository, "Database Admin",     "Quản trị cơ sở dữ liệu",           depts.get("IT")));

        // Phòng Marketing
        result.put("TP_MKT",       save(positionRepository, "Trưởng Phòng Marketing", "Quản lý hoạt động marketing",   depts.get("MARKETING")));
        result.put("CV_MKT",       save(positionRepository, "Chuyên Viên Marketing",   "Thực hiện chiến dịch marketing", depts.get("MARKETING")));
        result.put("CONTENT",      save(positionRepository, "Content Creator",         "Tạo nội dung truyền thông",      depts.get("MARKETING")));

        log.info("[DataSeeder] ✓ {} positions", result.size());
        return result;
    }

    private Position save(PositionRepository repo, String name, String desc, Department dept) {
        return repo.save(Position.builder().name(name).description(desc).department(dept).build());
    }

    // ─── 5. Users ─────────────────────────────────────────────────────────────

    private Map<String, User> seedUsers(Map<String, Role> roles) {
        String hash = passwordEncoder.encode(DEFAULT_PASSWORD);
        Map<String, User> result = new LinkedHashMap<>();

        List<Object[]> defs = List.of(
                // username,         email,                          roleName
                new Object[]{"admin",         "admin@company.com",           "ADMIN"},
                new Object[]{"hr_manager",    "hr@company.com",              "HR_MANAGER"},
                new Object[]{"it_manager",    "it.manager@company.com",      "DEPARTMENT_MANAGER"},
                new Object[]{"sales_manager", "sales.manager@company.com",   "DEPARTMENT_MANAGER"},
                new Object[]{"hr_lead",       "hr.lead@company.com",         "DEPARTMENT_MANAGER"},
                new Object[]{"employee1",     "nguyen.van.a@company.com",    "EMPLOYEE"},
                new Object[]{"employee2",     "tran.thi.b@company.com",      "EMPLOYEE"},
                new Object[]{"employee3",     "le.van.c@company.com",        "EMPLOYEE"},
                new Object[]{"employee4",     "pham.thi.d@company.com",      "EMPLOYEE"},
                new Object[]{"employee5",     "hoang.van.e@company.com",     "EMPLOYEE"},
                new Object[]{"employee6",     "vu.thi.f@company.com",        "EMPLOYEE"},
                new Object[]{"employee7",     "do.van.g@company.com",        "EMPLOYEE"},
                new Object[]{"employee8",     "nguyen.thi.h@company.com",    "EMPLOYEE"}
        );

        for (Object[] def : defs) {
            User u = User.builder()
                    .username((String) def[0])
                    .password(hash)
                    .email((String) def[1])
                    .role(roles.get((String) def[2]))
                    .status(UserStatus.ACTIVE)
                    .build();
            result.put((String) def[0], userRepository.save(u));
        }
        log.info("[DataSeeder] ✓ {} users (password: {})", result.size(), DEFAULT_PASSWORD);
        return result;
    }

    // ─── 6. Employees ─────────────────────────────────────────────────────────

    private Map<String, Employee> seedEmployees(Map<String, User> users,
                                                Map<String, Department> depts,
                                                Map<String, Position> positions) {
        Map<String, Employee> result = new LinkedHashMap<>();

        // username → { fullName, gender, birthDate, phone, address, deptKey, posKey }
        List<Object[]> defs = List.of(
                new Object[]{"admin",         "Nguyễn Văn Admin",   Gender.MALE,   "1980-01-15", "0901000001", "1 Lê Lợi, Q1, TP.HCM",                  "BGD",       "TRUONG_PHONG"},
                new Object[]{"hr_manager",    "Trần Thị Hương",     Gender.FEMALE, "1988-03-20", "0901000002", "2 Nguyễn Huệ, Q1, TP.HCM",               "HR",        "TP_HR"},
                new Object[]{"it_manager",    "Lê Minh Đức",        Gender.MALE,   "1985-07-10", "0901000003", "3 Cách Mạng Tháng 8, Q10, TP.HCM",       "IT",        "TP_IT"},
                new Object[]{"sales_manager", "Phạm Thị Lan",       Gender.FEMALE, "1990-09-15", "0901000004", "4 Phạm Ngũ Lão, Q1, TP.HCM",             "KINHDOANH", "TP_KD"},
                new Object[]{"hr_lead",       "Hoàng Văn Bình",     Gender.MALE,   "1987-11-05", "0901000005", "5 Lê Văn Sỹ, Q3, TP.HCM",                "HR",        "TP_HR"},
                new Object[]{"employee1",     "Nguyễn Văn An",      Gender.MALE,   "1996-02-28", "0901000006", "6 Nguyễn Trãi, Q5, TP.HCM",              "IT",        "DEVELOPER"},
                new Object[]{"employee2",     "Trần Thị Bình",      Gender.FEMALE, "1997-04-12", "0901000007", "7 Lê Văn Sỹ, Q3, TP.HCM",               "HR",        "CV_HR"},
                new Object[]{"employee3",     "Lê Văn Cường",       Gender.MALE,   "1995-06-18", "0901000008", "8 Xô Viết Nghệ Tĩnh, Q Bình Thạnh",      "KETOAN",    "KE_TOAN_VIEN"},
                new Object[]{"employee4",     "Phạm Thị Dung",      Gender.FEMALE, "1998-08-22", "0901000009", "9 Lý Thường Kiệt, Q Tân Bình",           "KINHDOANH", "CV_KD"},
                new Object[]{"employee5",     "Hoàng Văn Em",       Gender.MALE,   "1999-11-30", "0901000010", "10 Hoàng Văn Thụ, Q Phú Nhuận",          "MARKETING", "CV_MKT"},
                new Object[]{"employee6",     "Vũ Thị Phương",      Gender.FEMALE, "1997-05-15", "0901000011", "11 Đinh Tiên Hoàng, Q1, TP.HCM",         "IT",        "DEVELOPER"},
                new Object[]{"employee7",     "Đỗ Văn Quân",        Gender.MALE,   "1996-09-20", "0901000012", "12 Trần Hưng Đạo, Q5, TP.HCM",           "IT",        "DEVELOPER"},
                new Object[]{"employee8",     "Nguyễn Thị Hà",      Gender.FEMALE, "1998-03-08", "0901000013", "13 Nguyễn Đình Chiểu, Q3, TP.HCM",       "IT",        "SYS_ADMIN"}
        );

        for (Object[] def : defs) {
            Employee emp = Employee.builder()
                    .user(users.get((String) def[0]))
                    .fullName((String) def[1])
                    .gender((Gender) def[2])
                    .birthDate(LocalDate.parse((String) def[3]))
                    .phone((String) def[4])
                    .address((String) def[5])
                    .department(depts.get((String) def[6]))
                    .position(positions.get((String) def[7]))
                    .workStatus(WorkStatus.WORKING)
                    .build();
            result.put((String) def[0], employeeRepository.save(emp));
        }
        log.info("[DataSeeder] ✓ {} employees", result.size());
        return result;
    }

    // ─── 7. Cập nhật trưởng phòng cho departments ─────────────────────────────

    private void updateDepartmentManagers(Map<String, Department> depts,
                                          Map<String, Employee> employees) {
        setManager(depts.get("BGD"),       employees.get("admin"));
        setManager(depts.get("HR"),        employees.get("hr_lead"));
        setManager(depts.get("IT"),        employees.get("it_manager"));
        setManager(depts.get("KINHDOANH"), employees.get("sales_manager"));
        departmentRepository.saveAll(depts.values());
        log.info("[DataSeeder] ✓ Department managers updated");
    }

    private void setManager(Department dept, Employee manager) {
        dept.setManager(manager);
    }

    // ─── 8. Shifts ────────────────────────────────────────────────────────────

    private void seedShifts() {
        List<Shift> shifts = List.of(
                shift("Ca Sáng",    LocalTime.of(6, 0),  LocalTime.of(14, 0)),
                shift("Ca Chiều",   LocalTime.of(14, 0), LocalTime.of(22, 0)),
                shift("Hành Chính", LocalTime.of(8, 0),  LocalTime.of(17, 0))
        );
        shiftsRepository.saveAll(shifts);
        log.info("[DataSeeder] ✓ {} shifts", shifts.size());
    }

    private Shift shift(String name, LocalTime start, LocalTime end) {
        Shift s = new Shift();
        s.setShiftName(name);
        s.setStartTime(start);
        s.setEndTime(end);
        return s;
    }

    // ─── 9. Leave Types ───────────────────────────────────────────────────────

    private void seedLeaveTypes() {
        List<LeaveType> types = List.of(
                leaveType("Nghỉ phép năm",    true),
                leaveType("Nghỉ bệnh",        true),
                leaveType("Nghỉ việc riêng",  false),
                leaveType("Nghỉ thai sản",    true),
                leaveType("Nghỉ không lương", false)
        );
        leaveTypeRepository.saveAll(types);
        log.info("[DataSeeder] ✓ {} leave types", types.size());
    }

    private LeaveType leaveType(String name, boolean isPaid) {
        return LeaveType.builder().typeName(name).isPaid(isPaid).build();
    }

    // ─── 10. Monthly Schedules ────────────────────────────────────────────────

    private void seedMonthlySchedules(Map<String, Employee> employees) {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year  = now.getYear();

        List<String> itEmployees = List.of("employee1", "employee6", "employee7", "employee8");
        List<MonthlySchedules> schedules = new ArrayList<>();
        for (String username : itEmployees) {
            schedules.add(MonthlySchedules.builder()
                    .employee(employees.get(username))
                    .month(month)
                    .year(year)
                    .status(ScheduleStatus.OPEN)
                    .build());
        }
        monthlySchedulesRepository.saveAll(schedules);
        log.info("[DataSeeder] ✓ {} monthly schedules ({}/{})", schedules.size(), month, year);
    }

    // ─── 11. Contracts ────────────────────────────────────────────────────────

    private void seedContracts(Map<String, Employee> employees,
                               Map<String, Position> positions) {
        List<Contract> contracts = new ArrayList<>();

        Employee emp1 = employees.get("employee1"); // IT Developer
        Employee emp2 = employees.get("employee2"); // HR
        Employee emp3 = employees.get("employee3"); // Kế Toán
        Employee emp4 = employees.get("employee4"); // Kinh Doanh
        Employee emp5 = employees.get("employee5"); // Marketing

        // employee1: hợp đồng cũ đã hết hạn + hợp đồng mới đang active
        contracts.add(contract(emp1, Type.YEAR_1,     "Developer",
                LocalDate.of(2024, 1, 1),  LocalDate.of(2025, 1, 1),
                new BigDecimal("15000000"), ContractStatus.EXPIRED));
        contracts.add(contract(emp1, Type.YEAR_3,     "Developer",
                LocalDate.of(2025, 1, 2),  LocalDate.of(2028, 1, 2),
                new BigDecimal("18000000"), ContractStatus.ACTIVE));

        // employee2
        contracts.add(contract(emp2, Type.YEAR_1,     "Chuyên Viên Nhân Sự",
                LocalDate.of(2025, 3, 1),  LocalDate.of(2026, 3, 1),
                new BigDecimal("14000000"), ContractStatus.ACTIVE));

        // employee3
        contracts.add(contract(emp3, Type.INDEFINITE, "Kế Toán Viên",
                LocalDate.of(2022, 6, 1),  null,
                new BigDecimal("20000000"), ContractStatus.ACTIVE));

        // employee4
        contracts.add(contract(emp4, Type.YEAR_1,     "Chuyên Viên Kinh Doanh",
                LocalDate.of(2025, 9, 1),  LocalDate.of(2026, 9, 1),
                new BigDecimal("13000000"), ContractStatus.ACTIVE));

        // employee5
        contracts.add(contract(emp5, Type.YEAR_3,     "Chuyên Viên Marketing",
                LocalDate.of(2024, 12, 1), LocalDate.of(2027, 12, 1),
                new BigDecimal("16000000"), ContractStatus.ACTIVE));

        contractsRepository.saveAll(contracts);
        log.info("[DataSeeder] ✓ {} contracts", contracts.size());
    }

    private Contract contract(Employee emp, Type type, String position,
                              LocalDate startDate, LocalDate endDate,
                              BigDecimal salary, ContractStatus status) {
        return Contract.builder()
                .employee(emp)
                .type(type)
                .position(position)
                .startDate(startDate)
                .endDate(endDate)
                .baseSalary(salary)
                .status(status)
                .build();
    }
}
