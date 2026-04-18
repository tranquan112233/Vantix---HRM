package poly.edu.vantix_hrm.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import poly.edu.vantix_hrm.entity.Salary.SalaryStatus;
import poly.edu.vantix_hrm.entity.User.UserStatus;
import poly.edu.vantix_hrm.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

/*
 * DataSeeder
 * -------------------------------------------
 * Tự động khởi tạo dữ liệu mẫu khi ứng dụng khởi động.
 * Chỉ chạy một lần — nếu đã có permission trong DB thì bỏ qua.
 *
 * Thứ tự seed:
 *   1. Permissions  → 2. Roles  → 3. Departments  → 4. Positions
 *   → 5. Shifts  → 6. Leave Types
 *   → 7. Special accounts (6 người: admin + 4 managers + attendance test user)
 *   → 8. Bulk employees (3 000 người phân bổ theo phòng ban)
 *   → 9. Contracts  → 10. Monthly Schedules → 11. Attendance test data
 *   → 12. Salaries (3 tháng)
 *
 * Tổng: ~3 006 nhân viên, ~3 006 hợp đồng, ~6 012 lịch tháng, ~9 018 bảng lương
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final PermissionRepository       permissionRepository;
    private final RoleRepository             roleRepository;
    private final DepartmentRepository       departmentRepository;
    private final PositionRepository         positionRepository;
    private final UserRepository             userRepository;
    private final EmployeeRepository         employeeRepository;
    private final ShiftsRepository           shiftsRepository;
    private final LeaveTypeRepository        leaveTypeRepository;
    private final MonthlySchedulesRepository monthlySchedulesRepository;
    private final DailyWorkSchedulesRepository dailyWorkSchedulesRepository;
    private final AttendanceRepository       attendanceRepository;
    private final ContractsRepository        contractsRepository;
    private final SalariesRepository         salariesRepository;
    private final PasswordEncoder            passwordEncoder;

    @PersistenceContext
    private EntityManager em;

    private static final String DEFAULT_PASSWORD = "123456";
    private static final int    TOTAL_BULK       = 3000;
    private static final int    BATCH_SIZE       = 250;
    private static final ZoneId VIETNAM_ZONE     = ZoneId.of("Asia/Ho_Chi_Minh");

    private final Random rng = new Random(42); // seeded → reproducible data

    // ── Vietnamese name pools ──────────────────────────────────────────────────
    private static final String[] LAST   = {
        "Nguyễn","Trần","Lê","Phạm","Hoàng","Huỳnh","Phan","Vũ","Võ","Đặng",
        "Bùi","Đỗ","Hồ","Ngô","Dương","Lý","Đinh","Trương","Tô","Mai",
        "Đào","Lưu","Hà","Lâm","Tạ"
    };
    private static final String[] MID_M  = {"Văn","Quốc","Minh","Đức","Trung","Hoàng","Thanh","Hữu","Bá"};
    private static final String[] MID_F  = {"Thị","Ngọc","Thùy","Bích","Kim","Thu","Thanh","Tuyết","Hồng"};
    private static final String[] FST_M  = {
        "Minh","Hùng","Đức","Tuấn","Anh","Bình","Cường","Dũng","Hải","Khoa",
        "Long","Nam","Phong","Quân","Sơn","Thành","Vinh","Tùng","Lâm","Khánh",
        "Huy","Giang","Tiến","Bảo","Trung","Hiếu","Đạt","Phúc","Duy","Kiên"
    };
    private static final String[] FST_F  = {
        "Lan","Hoa","Mai","Hương","Linh","Nga","Nhung","Oanh","Phương","Quỳnh",
        "Thu","Thúy","Trang","Uyên","Vân","Yến","Thảo","Hạnh","Diệu","Tuyết",
        "Như","Ly","Nhi","Hà","Ngân","Chi","Châu","Hiền","Lý","Thư"
    };
    private static final String[] STREETS = {
        "Lê Lợi","Nguyễn Huệ","Trần Hưng Đạo","Lý Thường Kiệt","Cách Mạng Tháng 8",
        "Nguyễn Trãi","Đinh Tiên Hoàng","Lê Văn Sỹ","Hoàng Văn Thụ",
        "Xô Viết Nghệ Tĩnh","Võ Văn Tần","Nam Kỳ Khởi Nghĩa","Hai Bà Trưng",
        "Tôn Đức Thắng","Pasteur"
    };
    private static final String[] DISTS  = {
        "Quận 1","Quận 3","Quận 5","Quận 7","Quận 10","Quận 12",
        "Bình Thạnh","Gò Vấp","Tân Bình","Phú Nhuận","Thủ Đức"
    };

    // ─── Entry point ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (permissionRepository.count() > 0) {
            log.info("[DataSeeder] Dữ liệu đã tồn tại, bỏ qua seed.");
            return;
        }
        log.info("[DataSeeder] Bắt đầu seed dữ liệu ({} nhân viên)...", TOTAL_BULK + 5);

        // ── Phase 1: Reference data ───────────────────────────────────────────
        Map<String, Permission> perms     = seedPermissions();
        Map<String, Role>       roles     = seedRoles(perms);
        Map<String, Department> depts     = seedDepartments();
        Map<String, Position>   positions = seedPositions(depts);
        seedShifts();
        seedLeaveTypes();

        // ── Phase 2: Special accounts (5 người) ──────────────────────────────
        List<Long> allEmpIds = new ArrayList<>(TOTAL_BULK + 10);
        Map<String, Employee> specials = seedSpecialAccounts(roles, depts, positions);
        specials.values().forEach(e -> allEmpIds.add(e.getId()));
        updateDepartmentManagers(depts, specials);

        // Flush phase 1+2 vào DB, sau đó clear persistence context
        em.flush();
        em.clear();

        // Lưu ID trước khi entity bị detach
        Map<String, Long> deptIds = new HashMap<>();
        depts.forEach((k, v) -> deptIds.put(k, v.getId()));

        Map<String, Long> posIds = new HashMap<>();
        positions.forEach((k, v) -> posIds.put(k, v.getId()));

        Long empRoleId = roles.get("EMPLOYEE").getId();

        // ── Phase 3: Bulk employees (3 000 người) ────────────────────────────
        List<Long> bulkIds = seedBulkEmployees(deptIds, posIds, empRoleId);
        allEmpIds.addAll(bulkIds);

        // ── Phase 4: Contracts ────────────────────────────────────────────────
        Map<Long, BigDecimal> empSalaryMap = seedContractsForAll(allEmpIds);

        // ── Phase 5: Monthly schedules (tháng trước LOCKED + tháng này OPEN) ─
        seedMonthlySchedulesForAll(allEmpIds);

        // ── Phase 6: Attendance test data ────────────────────────────────────
        seedAttendanceTestData(specials);

        // ── Phase 7: Salaries (Jan-Mar 2026, trạng thái APPROVED) ────────────
        seedSalariesForAll(allEmpIds, empSalaryMap);

        log.info("[DataSeeder] Hoàn tất! {} nhân viên — password: {}", allEmpIds.size(), DEFAULT_PASSWORD);
    }

    // ─── 1. Permissions ───────────────────────────────────────────────────────

    private Map<String, Permission> seedPermissions() {
        List<Object[]> defs = List.of(
                new Object[]{"USER_VIEW",                    "Xem thông tin người dùng"},
                new Object[]{"USER_CREATE",                  "Tạo người dùng mới"},
                new Object[]{"USER_UPDATE",                  "Cập nhật thông tin người dùng"},
                new Object[]{"USER_DELETE",                  "Xóa người dùng"},
                new Object[]{"USER_STATUS_UPDATE",           "Thay đổi trạng thái tài khoản"},
                new Object[]{"PERMISSION_VIEW",              "Xem danh sách quyền"},
                new Object[]{"PERMISSION_CREATE",            "Tạo quyền mới"},
                new Object[]{"PERMISSION_UPDATE",            "Cập nhật quyền"},
                new Object[]{"PERMISSION_DELETE",            "Xóa quyền"},
                new Object[]{"ROLE_VIEW",                    "Xem danh sách vai trò"},
                new Object[]{"ROLE_CREATE",                  "Tạo vai trò mới"},
                new Object[]{"ROLE_UPDATE",                  "Cập nhật vai trò"},
                new Object[]{"ROLE_DELETE",                  "Xóa vai trò"},
                new Object[]{"EMPLOYEE_VIEW",                "Xem thông tin nhân viên"},
                new Object[]{"EMPLOYEE_CREATE",              "Tạo nhân viên mới"},
                new Object[]{"EMPLOYEE_UPDATE",              "Cập nhật thông tin nhân viên"},
                new Object[]{"EMPLOYEE_DELETE",              "Xóa nhân viên"},
                new Object[]{"DEPARTMENT_VIEW",              "Xem thông tin phòng ban"},
                new Object[]{"DEPARTMENT_CREATE",            "Tạo phòng ban mới"},
                new Object[]{"DEPARTMENT_UPDATE",            "Cập nhật phòng ban"},
                new Object[]{"DEPARTMENT_DELETE",            "Xóa phòng ban"},
                new Object[]{"POSITION_VIEW",                "Xem thông tin vị trí"},
                new Object[]{"POSITION_CREATE",              "Tạo vị trí mới"},
                new Object[]{"POSITION_UPDATE",              "Cập nhật vị trí"},
                new Object[]{"POSITION_DELETE",              "Xóa vị trí"},
                new Object[]{"ATTENDANCE_VIEW",              "Xem bảng chấm công"},
                new Object[]{"ATTENDANCE_CHECKIN",           "Chấm công vào ca"},
                new Object[]{"ATTENDANCE_CHECKOUT",          "Chấm công ra ca"},
                new Object[]{"ATTENDANCE_MANAGEMENT_VIEW",   "Xem danh sách chấm công cần duyệt"},
                new Object[]{"ATTENDANCE_MANAGEMENT_APPROVE","Duyệt/Từ chối chấm công"},
                new Object[]{"LEAVE_TYPE_VIEW",              "Xem danh sách loại nghỉ phép"},
                new Object[]{"LEAVE_TYPE_CREATE",            "Tạo loại nghỉ phép mới"},
                new Object[]{"LEAVE_TYPE_UPDATE",            "Cập nhật loại nghỉ phép"},
                new Object[]{"LEAVE_TYPE_DELETE",            "Xóa loại nghỉ phép"},
                new Object[]{"LEAVE_VIEW",                   "Xem đơn xin nghỉ của bản thân"},
                new Object[]{"LEAVE_CREATE",                 "Nộp đơn xin nghỉ"},
                new Object[]{"LEAVE_MANAGE",                 "Duyệt/Từ chối đơn xin nghỉ"},
                new Object[]{"CONTRACT_VIEW",                "Xem danh sách hợp đồng"},
                new Object[]{"CONTRACT_CREATE",              "Tạo hợp đồng mới"},
                new Object[]{"CONTRACT_DELETE",              "Xóa hợp đồng"},
                new Object[]{"CONTRACT_STATUS_UPDATE",       "Cập nhật trạng thái hợp đồng"},
                new Object[]{"CONTRACT_ANNEX_VIEW",          "Xem phụ lục hợp đồng"},
                new Object[]{"CONTRACT_ANNEX_CREATE",        "Tạo phụ lục hợp đồng mới"},
                new Object[]{"CONTRACT_ANNEX_UPDATE",        "Cập nhật phụ lục hợp đồng"},
                new Object[]{"CONTRACT_ANNEX_DELETE",        "Xóa phụ lục hợp đồng"},
                new Object[]{"SCHEDULE_VIEW",                "Xem lịch làm việc"},
                new Object[]{"SCHEDULE_CREATE",              "Phân ca làm việc"},
                new Object[]{"SCHEDULE_STATUS_UPDATE",       "Cập nhật trạng thái lịch làm việc"},
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
        Role admin = buildRole("ADMIN", "Quản trị viên hệ thống - có tất cả quyền",
                new HashSet<>(perms.values()));

        Role hrManager = buildRole("HR_MANAGER", "Quản lý nhân sự", permissions(perms,
                "USER_VIEW","USER_CREATE","USER_UPDATE","USER_DELETE","USER_STATUS_UPDATE",
                "EMPLOYEE_VIEW","EMPLOYEE_CREATE","EMPLOYEE_UPDATE","EMPLOYEE_DELETE",
                "DEPARTMENT_VIEW","DEPARTMENT_CREATE","DEPARTMENT_UPDATE","DEPARTMENT_DELETE",
                "POSITION_VIEW","POSITION_CREATE","POSITION_UPDATE","POSITION_DELETE",
                "ATTENDANCE_VIEW","ATTENDANCE_MANAGEMENT_VIEW","ATTENDANCE_MANAGEMENT_APPROVE",
                "LEAVE_TYPE_VIEW","LEAVE_TYPE_CREATE","LEAVE_TYPE_UPDATE","LEAVE_TYPE_DELETE",
                "LEAVE_VIEW","LEAVE_MANAGE",
                "CONTRACT_VIEW","CONTRACT_CREATE","CONTRACT_DELETE","CONTRACT_STATUS_UPDATE",
                "CONTRACT_ANNEX_VIEW","CONTRACT_ANNEX_CREATE","CONTRACT_ANNEX_UPDATE","CONTRACT_ANNEX_DELETE",
                "SCHEDULE_VIEW","SCHEDULE_CREATE","SCHEDULE_STATUS_UPDATE",
                "SALARY_VIEW","REPORT_VIEW"
        ));

        Role deptManager = buildRole("DEPARTMENT_MANAGER", "Trưởng phòng - quản lý nhân viên trong phòng",
                permissions(perms,
                        "EMPLOYEE_VIEW","EMPLOYEE_UPDATE",
                        "DEPARTMENT_VIEW","POSITION_VIEW",
                        "ATTENDANCE_VIEW","ATTENDANCE_MANAGEMENT_VIEW","ATTENDANCE_MANAGEMENT_APPROVE",
                        "LEAVE_TYPE_VIEW","LEAVE_VIEW","LEAVE_MANAGE",
                        "CONTRACT_VIEW","CONTRACT_ANNEX_VIEW",
                        "SCHEDULE_VIEW","SCHEDULE_CREATE","SCHEDULE_STATUS_UPDATE"
                ));

        Role employee = buildRole("EMPLOYEE", "Nhân viên - quyền cơ bản", permissions(perms,
                "EMPLOYEE_VIEW","DEPARTMENT_VIEW","POSITION_VIEW",
                "ATTENDANCE_VIEW","ATTENDANCE_CHECKIN","ATTENDANCE_CHECKOUT",
                "LEAVE_TYPE_VIEW","LEAVE_VIEW","LEAVE_CREATE",
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

    // ─── 3. Departments ───────────────────────────────────────────────────────

    private Map<String, Department> seedDepartments() {
        Map<String, Department> result = new LinkedHashMap<>();
        List<Object[]> defs = List.of(
                new Object[]{"BGD",       "Ban Giám Đốc",       "Quản lý điều hành công ty"},
                new Object[]{"HR",        "Phòng Nhân Sự",      "Quản lý nhân sự, tuyển dụng, đào tạo"},
                new Object[]{"KETOAN",    "Phòng Kế Toán",      "Quản lý tài chính, kế toán, lương thưởng"},
                new Object[]{"KINHDOANH", "Phòng Kinh Doanh",   "Phát triển thị trường, bán hàng"},
                new Object[]{"IT",        "Phòng IT",           "Quản trị hệ thống, phát triển phần mềm"},
                new Object[]{"MARKETING", "Phòng Marketing",    "Quảng bá thương hiệu, tiếp thị sản phẩm"}
        );
        for (Object[] def : defs) {
            result.put((String) def[0], departmentRepository.save(
                    Department.builder().name((String) def[1]).description((String) def[2]).build()));
        }
        log.info("[DataSeeder] ✓ {} departments", result.size());
        return result;
    }

    // ─── 4. Positions ─────────────────────────────────────────────────────────

    private Map<String, Position> seedPositions(Map<String, Department> depts) {
        Map<String, Position> r = new LinkedHashMap<>();

        // BGD
        r.put("TRUONG_PHONG", pos("Trưởng Phòng",             "Trưởng phòng ban",                        depts.get("BGD")));
        r.put("PHO_GD",       pos("Phó Giám Đốc",             "Hỗ trợ Giám đốc điều hành",               depts.get("BGD")));
        // HR
        r.put("TP_HR",        pos("Trưởng Phòng Nhân Sự",     "Quản lý toàn bộ hoạt động nhân sự",       depts.get("HR")));
        r.put("CV_HR",        pos("Chuyên Viên Nhân Sự",      "Thực hiện nghiệp vụ nhân sự",             depts.get("HR")));
        r.put("NV_TUYENDUNG", pos("Nhân Viên Tuyển Dụng",     "Phụ trách tuyển dụng",                    depts.get("HR")));
        // Kế Toán
        r.put("TP_KT",          pos("Trưởng Phòng Kế Toán",  "Quản lý tài chính kế toán",               depts.get("KETOAN")));
        r.put("KE_TOAN_TRUONG", pos("Kế Toán Trưởng",         "Phụ trách kế toán tổng hợp",              depts.get("KETOAN")));
        r.put("KE_TOAN_VIEN",   pos("Kế Toán Viên",           "Thực hiện công việc kế toán",             depts.get("KETOAN")));
        // Kinh Doanh
        r.put("TP_KD",       pos("Trưởng Phòng Kinh Doanh",  "Quản lý hoạt động kinh doanh",            depts.get("KINHDOANH")));
        r.put("CV_KD",       pos("Chuyên Viên Kinh Doanh",   "Phát triển khách hàng",                   depts.get("KINHDOANH")));
        r.put("NV_BANHANG",  pos("Nhân Viên Bán Hàng",       "Tư vấn và bán hàng",                      depts.get("KINHDOANH")));
        // IT
        r.put("TP_IT",    pos("Trưởng Phòng IT",   "Quản lý hệ thống và đội ngũ IT",                    depts.get("IT")));
        r.put("DEVELOPER",pos("Developer",          "Phát triển phần mềm",                               depts.get("IT")));
        r.put("SYS_ADMIN",pos("System Admin",       "Quản trị hệ thống",                                 depts.get("IT")));
        r.put("DBA",      pos("Database Admin",     "Quản trị cơ sở dữ liệu",                            depts.get("IT")));
        // Marketing
        r.put("TP_MKT",  pos("Trưởng Phòng Marketing", "Quản lý hoạt động marketing",                   depts.get("MARKETING")));
        r.put("CV_MKT",  pos("Chuyên Viên Marketing",   "Thực hiện chiến dịch marketing",                depts.get("MARKETING")));
        r.put("CONTENT", pos("Content Creator",         "Tạo nội dung truyền thông",                     depts.get("MARKETING")));

        log.info("[DataSeeder] ✓ {} positions", r.size());
        return r;
    }

    // ─── 5. Shifts ────────────────────────────────────────────────────────────

    private void seedShifts() {
        LocalTime now = LocalTime.now(VIETNAM_ZONE);
        LocalTime testStart = now.isBefore(LocalTime.of(0, 20))
                ? LocalTime.MIDNIGHT
                : now.minusMinutes(5);
        LocalTime testEnd = now.isAfter(LocalTime.of(22, 0))
                ? LocalTime.of(23, 59)
                : now.plusHours(2);

        shiftsRepository.saveAll(List.of(
                shift("Ca Sáng",    LocalTime.of(6,  0), LocalTime.of(14, 0)),
                shift("Ca Chiều",   LocalTime.of(14, 0), LocalTime.of(22, 0)),
                shift("Hành Chính", LocalTime.of(8,  0), LocalTime.of(17, 0)),
                shift("Ca Test Chấm Công", testStart, testEnd)
        ));
        log.info("[DataSeeder] ✓ 4 shifts");
    }

    // ─── 6. Leave Types ───────────────────────────────────────────────────────

    private void seedLeaveTypes() {
        leaveTypeRepository.saveAll(List.of(
                LeaveType.builder().typeName("Nghỉ phép năm").isPaid(true).build(),
                LeaveType.builder().typeName("Nghỉ bệnh").isPaid(true).build(),
                LeaveType.builder().typeName("Nghỉ việc riêng").isPaid(false).build(),
                LeaveType.builder().typeName("Nghỉ thai sản").isPaid(true).build(),
                LeaveType.builder().typeName("Nghỉ không lương").isPaid(false).build()
        ));
        log.info("[DataSeeder] ✓ 5 leave types");
    }

    // ─── 7. Special accounts ──────────────────────────────────────────────────

    private Map<String, Employee> seedSpecialAccounts(Map<String, Role> roles,
                                                       Map<String, Department> depts,
                                                       Map<String, Position> positions) {
        String hash = passwordEncoder.encode(DEFAULT_PASSWORD);
        Map<String, Employee> result = new LinkedHashMap<>();

        // username, email, roleKey, fullName, gender, birthDate, phone, address, deptKey, posKey
        List<Object[]> defs = List.of(
            new Object[]{"admin",         "admin@company.com",         "ADMIN",              "Nguyễn Văn Admin", Gender.MALE,   "1980-01-15", "0901000001", "1 Lê Lợi, Q1, TP.HCM",             "BGD",       "TRUONG_PHONG"},
            new Object[]{"hr_manager",    "hr@company.com",            "HR_MANAGER",         "Trần Thị Hương",   Gender.FEMALE, "1988-03-20", "0901000002", "2 Nguyễn Huệ, Q1, TP.HCM",         "HR",        "TP_HR"},
            new Object[]{"it_manager",    "it.manager@company.com",    "DEPARTMENT_MANAGER", "Lê Minh Đức",      Gender.MALE,   "1985-07-10", "0901000003", "3 CMT8, Q10, TP.HCM",               "IT",        "TP_IT"},
            new Object[]{"sales_manager", "sales.manager@company.com", "DEPARTMENT_MANAGER", "Phạm Thị Lan",     Gender.FEMALE, "1990-09-15", "0901000004", "4 Phạm Ngũ Lão, Q1, TP.HCM",       "KINHDOANH", "TP_KD"},
            new Object[]{"hr_lead",       "hr.lead@company.com",       "DEPARTMENT_MANAGER", "Hoàng Văn Bình",   Gender.MALE,   "1987-11-05", "0901000005", "5 Lê Văn Sỹ, Q3, TP.HCM",          "HR",        "TP_HR"},
            new Object[]{"attendance_staff","attendance@company.com",   "EMPLOYEE",           "Nguyễn Test Công", Gender.MALE,   "1996-04-18", "0901000006", "6 Hai Bà Trưng, Q1, TP.HCM",      "HR",        "CV_HR"}
        );

        for (Object[] d : defs) {
            User u = userRepository.save(User.builder()
                    .username((String) d[0]).password(hash).email((String) d[1])
                    .role(roles.get((String) d[2])).status(UserStatus.ACTIVE).build());

            result.put((String) d[0], employeeRepository.save(Employee.builder()
                    .user(u).fullName((String) d[3]).gender((Gender) d[4])
                    .birthDate(LocalDate.parse((String) d[5])).phone((String) d[6]).address((String) d[7])
                    .department(depts.get((String) d[8])).position(positions.get((String) d[9]))
                    .workStatus(WorkStatus.WORKING).build()));
        }
        log.info("[DataSeeder] ✓ {} special accounts", result.size());
        return result;
    }

    // ─── 8. Department managers ───────────────────────────────────────────────

    private void updateDepartmentManagers(Map<String, Department> depts,
                                           Map<String, Employee> specials) {
        depts.get("BGD").setManager(specials.get("admin"));
        depts.get("HR").setManager(specials.get("hr_lead"));
        depts.get("IT").setManager(specials.get("it_manager"));
        depts.get("KINHDOANH").setManager(specials.get("sales_manager"));
        departmentRepository.saveAll(depts.values());
        log.info("[DataSeeder] ✓ Department managers updated");
    }

    // ─── 9. Bulk employees (3 000 người) ─────────────────────────────────────
    /*
     * Phân bổ:  BGD=50  HR=400  KETOAN=450  KINHDOANH=750  IT=800  MARKETING=550
     * Mỗi batch: lưu users → lưu employees → flush/clear (tránh OOM)
     * Dùng em.getReference() thay vì entity trực tiếp sau khi clear
     */
    private List<Long> seedBulkEmployees(Map<String, Long> deptIds,
                                          Map<String, Long> posIds,
                                          Long empRoleId) {
        String hash = passwordEncoder.encode(DEFAULT_PASSWORD);

        record DeptCfg(String deptKey, int count, List<String> posKeys) {}
        List<DeptCfg> configs = List.of(
            new DeptCfg("BGD",        50,  List.of("PHO_GD")),
            new DeptCfg("HR",         400, List.of("CV_HR","CV_HR","NV_TUYENDUNG")),
            new DeptCfg("KETOAN",     450, List.of("KE_TOAN_VIEN","KE_TOAN_VIEN","KE_TOAN_TRUONG")),
            new DeptCfg("KINHDOANH",  750, List.of("NV_BANHANG","NV_BANHANG","CV_KD")),
            new DeptCfg("IT",         800, List.of("DEVELOPER","DEVELOPER","DEVELOPER","SYS_ADMIN","DBA")),
            new DeptCfg("MARKETING",  550, List.of("CV_MKT","CV_MKT","CONTENT"))
        );
        // Tổng: 50+400+450+750+800+550 = 3 000

        List<Long> allIds = new ArrayList<>(TOTAL_BULK);
        int seq = 10000; // username emp10000..emp12999, phone 0900010000..

        for (DeptCfg cfg : configs) {
            int done = 0;
            while (done < cfg.count) {
                int batchSize = Math.min(BATCH_SIZE, cfg.count - done);

                // Build user batch
                List<User> uBatch = new ArrayList<>(batchSize);
                // Store extra info per slot: [posKey, fullName, phone, address, isMale, birthDate]
                String[][] extras = new String[batchSize][6];

                for (int i = 0; i < batchSize; i++) {
                    boolean male   = rng.nextBoolean();
                    int     mySeq  = seq++;
                    String  uname  = "emp" + mySeq;

                    uBatch.add(User.builder()
                            .username(uname)
                            .password(hash)
                            .email(uname + "@company.com")
                            .role(em.getReference(Role.class, empRoleId))
                            .status(UserStatus.ACTIVE)
                            .build());

                    extras[i][0] = pick(cfg.posKeys);
                    extras[i][1] = randName(male);
                    extras[i][2] = fmtPhone(mySeq);
                    extras[i][3] = randAddr();
                    extras[i][4] = male ? "M" : "F";
                    extras[i][5] = randBirth();
                }

                List<User> savedUsers = userRepository.saveAll(uBatch);

                List<Employee> eBatch = new ArrayList<>(batchSize);
                for (int i = 0; i < batchSize; i++) {
                    Gender g = "M".equals(extras[i][4]) ? Gender.MALE : Gender.FEMALE;
                    eBatch.add(Employee.builder()
                            .user(savedUsers.get(i))
                            .fullName(extras[i][1])
                            .gender(g)
                            .birthDate(LocalDate.parse(extras[i][5]))
                            .phone(extras[i][2])
                            .address(extras[i][3])
                            .department(em.getReference(Department.class, deptIds.get(cfg.deptKey)))
                            .position(em.getReference(Position.class, posIds.get(extras[i][0])))
                            .workStatus(rng.nextInt(20) == 0 ? WorkStatus.RESIGNED : WorkStatus.WORKING)
                            .build());
                }

                List<Employee> saved = employeeRepository.saveAll(eBatch);
                saved.forEach(e -> allIds.add(e.getId()));

                em.flush();
                em.clear();
                done += batchSize;
            }
            log.info("[DataSeeder]   → {} employees / {}", cfg.count, cfg.deptKey);
        }
        log.info("[DataSeeder] ✓ {} bulk employees", allIds.size());
        return allIds;
    }

    // ─── 10. Contracts for ALL employees ─────────────────────────────────────
    /*
     * Mỗi nhân viên có 1 hợp đồng (đa số ACTIVE, ~15% EXPIRED).
     * Trả về map empId → baseSalary để dùng lại cho bảng lương.
     */
    private Map<Long, BigDecimal> seedContractsForAll(List<Long> empIds) {
        Map<Long, BigDecimal> salaryMap = new HashMap<>(empIds.size());
        List<Contract> batch = new ArrayList<>(BATCH_SIZE);
        LocalDate today = LocalDate.now();
        Type[] typePool = {Type.YEAR_1, Type.YEAR_1, Type.YEAR_3, Type.INDEFINITE};

        for (Long id : empIds) {
            BigDecimal salary = randSalary();
            salaryMap.put(id, salary);

            Type      type  = pick(typePool);
            LocalDate start = LocalDate.of(2019 + rng.nextInt(6), 1 + rng.nextInt(12), 1);
            LocalDate end   = switch (type) {
                case YEAR_1    -> start.plusYears(1);
                case YEAR_3    -> start.plusYears(3);
                case INDEFINITE-> null;
            };
            ContractStatus status = (end == null || end.isAfter(today))
                    ? ContractStatus.ACTIVE : ContractStatus.EXPIRED;

            batch.add(Contract.builder()
                    .employee(em.getReference(Employee.class, id))
                    .type(type)
                    .startDate(start)
                    .endDate(end)
                    .position("Nhân viên")
                    .baseSalary(salary)
                    .status(status)
                    .build());

            if (batch.size() >= BATCH_SIZE) {
                contractsRepository.saveAll(batch);
                em.flush(); em.clear();
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            contractsRepository.saveAll(batch);
            em.flush(); em.clear();
        }
        log.info("[DataSeeder] ✓ {} contracts", empIds.size());
        return salaryMap;
    }

    // ─── 11. Monthly schedules ────────────────────────────────────────────────
    /*
     * Tháng trước: LOCKED  |  Tháng hiện tại: OPEN
     */
    private void seedMonthlySchedulesForAll(List<Long> empIds) {
        LocalDate now      = LocalDate.now();
        LocalDate prevDate = now.minusMonths(1);
        int curM = now.getMonthValue(),  curY  = now.getYear();
        int preM = prevDate.getMonthValue(), preY = prevDate.getYear();

        List<MonthlySchedules> batch = new ArrayList<>(BATCH_SIZE * 2);

        for (Long id : empIds) {
            Employee ref = em.getReference(Employee.class, id);
            batch.add(MonthlySchedules.builder().employee(ref).month(preM).year(preY).status(ScheduleStatus.LOCKED).build());
            batch.add(MonthlySchedules.builder().employee(ref).month(curM).year(curY).status(ScheduleStatus.OPEN).build());

            if (batch.size() >= BATCH_SIZE) {
                monthlySchedulesRepository.saveAll(batch);
                em.flush(); em.clear();
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            monthlySchedulesRepository.saveAll(batch);
            em.flush(); em.clear();
        }
        log.info("[DataSeeder] ✓ {} monthly schedules (2 tháng/người)", empIds.size() * 2);
    }

    // ─── 12. Attendance test data ────────────────────────────────────────────
    /*
     * Tài khoản test:
     *   - attendance_staff / 123456: nhân viên HR dùng để check-in/check-out.
     *   - hr_lead / 123456: trưởng phòng HR dùng để duyệt phiếu PENDING.
     *
     * Dữ liệu tạo sẵn:
     *   - Hôm nay: có DailyWorkSchedules với "Ca Test Chấm Công", chưa có Attendance
     *     để người test tự bấm Check-in.
     *   - Hôm qua: Attendance PENDING để test màn Attendance Approval.
     *   - Hai ngày trước: Attendance APPROVED để test lịch chấm công cá nhân.
     */
    private void seedAttendanceTestData(Map<String, Employee> specials) {
        Employee staff = specials.get("attendance_staff");
        if (staff == null) {
            log.warn("[DataSeeder] Không tìm thấy attendance_staff, bỏ qua attendance test data.");
            return;
        }

        Shift testShift = findShiftByName("Ca Test Chấm Công");
        Shift officeShift = findShiftByName("Hành Chính");
        LocalDate today = LocalDate.now(VIETNAM_ZONE);
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);

        ensureDailySchedule(staff, today, testShift);
        ensureDailySchedule(staff, yesterday, officeShift);
        ensureDailySchedule(staff, twoDaysAgo, officeShift);

        attendanceRepository.save(Attendance.builder()
                .employee(staff)
                .shift(officeShift)
                .workDate(yesterday)
                .checkIn(LocalTime.of(8, 25))
                .checkOut(LocalTime.of(17, 0))
                .lateMinutes(25)
                .earlyLeaveMinutes(0)
                .status(Attendance.AttendanceStatus.PENDING)
                .build());

        attendanceRepository.save(Attendance.builder()
                .employee(staff)
                .shift(officeShift)
                .workDate(twoDaysAgo)
                .checkIn(LocalTime.of(7, 58))
                .checkOut(LocalTime.of(16, 45))
                .lateMinutes(0)
                .earlyLeaveMinutes(15)
                .status(Attendance.AttendanceStatus.APPROVED)
                .build());

        log.info("[DataSeeder] ✓ attendance test data for attendance_staff; manager: hr_lead");
    }

    // ─── 13. Salaries — Jan / Feb / Mar 2026 (APPROVED) ──────────────────────
    /*
     * Công thức đơn giản:
     *   earnedBase  = baseSalary × (actualDays / 22)
     *   totalIncome = earnedBase + allowance + bonus
     *   bhxh/bhyt/bhtn = 8% / 1.5% / 1% của baseSalary
     *   tax         = 10% × max(0, totalIncome - deductions - 11 000 000)
     *   netSalary   = totalIncome - totalDeduction
     */
    private void seedSalariesForAll(List<Long> empIds, Map<Long, BigDecimal> salaryMap) {
        int[][] salaryMonths = {{2026, 1}, {2026, 2}, {2026, 3}};
        int standard = 22;

        List<Salary> batch = new ArrayList<>(BATCH_SIZE * 3);

        for (Long id : empIds) {
            Employee ref  = em.getReference(Employee.class, id);
            BigDecimal base = salaryMap.getOrDefault(id, BigDecimal.valueOf(10_000_000));

            for (int[] ym : salaryMonths) {
                int        actual     = standard - rng.nextInt(5); // 17–22 ngày
                BigDecimal dailyRate  = base.divide(BigDecimal.valueOf(standard), 0, RoundingMode.HALF_UP);
                BigDecimal earnedBase = dailyRate.multiply(BigDecimal.valueOf(actual));
                BigDecimal allowance  = round100k(500_000L + (long) rng.nextInt(15) * 100_000L);
                BigDecimal bonus      = rng.nextInt(5) == 0
                        ? round100k(1_000_000L + (long) rng.nextInt(20) * 100_000L)
                        : BigDecimal.ZERO;

                BigDecimal bhxh = pct(base, 0.08);
                BigDecimal bhyt = pct(base, 0.015);
                BigDecimal bhtn = pct(base, 0.01);

                BigDecimal totalIncome    = earnedBase.add(allowance).add(bonus);
                BigDecimal preDeduction   = bhxh.add(bhyt).add(bhtn);
                BigDecimal taxable        = totalIncome.subtract(preDeduction).subtract(BigDecimal.valueOf(11_000_000));
                BigDecimal tax            = taxable.compareTo(BigDecimal.ZERO) > 0
                        ? pct(taxable, 0.10) : BigDecimal.ZERO;
                BigDecimal totalDeduction = preDeduction.add(tax);
                BigDecimal netSalary      = totalIncome.subtract(totalDeduction);

                batch.add(Salary.builder()
                        .employee(ref)
                        .salaryMonth(LocalDate.of(ym[0], ym[1], 1))
                        .baseSalarySnapshot(base)
                        .standardWorkDays(standard)
                        .actualWorkDays(actual)
                        .allowance(allowance)
                        .bonus(bonus)
                        .bhxhAmount(bhxh)
                        .bhytAmount(bhyt)
                        .bhtnAmount(bhtn)
                        .taxAmount(tax)
                        .totalIncome(totalIncome)
                        .totalDeduction(totalDeduction)
                        .netSalary(netSalary)
                        .status(SalaryStatus.APPROVED)
                        .build());
            }

            if (batch.size() >= BATCH_SIZE) {
                salariesRepository.saveAll(batch);
                em.flush(); em.clear();
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            salariesRepository.saveAll(batch);
            em.flush(); em.clear();
        }
        log.info("[DataSeeder] ✓ {} salary records (3 tháng/người)", empIds.size() * 3);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String randName(boolean male) {
        return male
                ? LAST[rng.nextInt(LAST.length)] + " " + MID_M[rng.nextInt(MID_M.length)] + " " + FST_M[rng.nextInt(FST_M.length)]
                : LAST[rng.nextInt(LAST.length)] + " " + MID_F[rng.nextInt(MID_F.length)] + " " + FST_F[rng.nextInt(FST_F.length)];
    }

    private String randAddr() {
        return (1 + rng.nextInt(200)) + " " + STREETS[rng.nextInt(STREETS.length)]
                + ", " + DISTS[rng.nextInt(DISTS.length)] + ", TP.HCM";
    }

    private String randBirth() {
        // 1970–1999
        return String.format("%04d-%02d-%02d", 1970 + rng.nextInt(30), 1 + rng.nextInt(12), 1 + rng.nextInt(28));
    }

    /** phone 10 ký tự: 09XXXXXXXX */
    private String fmtPhone(int seq) {
        return String.format("09%08d", seq % 100_000_000);
    }

    /** Lương ngẫu nhiên 8M – 44.5M, bội số 500k */
    private BigDecimal randSalary() {
        return BigDecimal.valueOf(8_000_000L + (long) rng.nextInt(74) * 500_000L);
    }

    /** Làm tròn lên bội số 100k */
    private BigDecimal round100k(long value) {
        return BigDecimal.valueOf(value / 100_000 * 100_000);
    }

    /** Tính phần trăm, làm tròn tới đồng */
    private BigDecimal pct(BigDecimal base, double rate) {
        return base.multiply(BigDecimal.valueOf(rate)).setScale(0, RoundingMode.HALF_UP);
    }

    private <T> T pick(List<T> list) {
        return list.get(rng.nextInt(list.size()));
    }

    private <T> T pick(T[] array) {
        return array[rng.nextInt(array.length)];
    }

    private Position pos(String name, String desc, Department dept) {
        return positionRepository.save(Position.builder().name(name).description(desc).department(dept).build());
    }

    private Role buildRole(String name, String desc, Set<Permission> perms) {
        return Role.builder().name(name).description(desc).permissions(perms).build();
    }

    private Set<Permission> permissions(Map<String, Permission> perms, String... names) {
        Set<Permission> set = new HashSet<>();
        for (String n : names) set.add(perms.get(n));
        return set;
    }

    private Shift shift(String name, LocalTime start, LocalTime end) {
        Shift s = new Shift();
        s.setShiftName(name);
        s.setStartTime(start);
        s.setEndTime(end);
        return s;
    }

    private Shift findShiftByName(String name) {
        return shiftsRepository.findAll().stream()
                .filter(s -> name.equals(s.getShiftName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy ca làm việc: " + name));
    }

    private void ensureDailySchedule(Employee employee, LocalDate workDate, Shift shift) {
        MonthlySchedules monthly = monthlySchedulesRepository.findByEmployee_IdAndMonthAndYear(
                employee.getId(), workDate.getMonthValue(), workDate.getYear());

        if (monthly == null) {
            monthly = monthlySchedulesRepository.save(MonthlySchedules.builder()
                    .employee(employee)
                    .month(workDate.getMonthValue())
                    .year(workDate.getYear())
                    .status(ScheduleStatus.OPEN)
                    .build());
        }

        DailyWorkSchedules schedule = dailyWorkSchedulesRepository
                .findByEmployeeIdAndWorkDate(employee.getId(), workDate)
                .orElseGet(DailyWorkSchedules::new);

        schedule.setMonthlySchedule(monthly);
        schedule.setWorkDate(workDate);
        schedule.setShift(shift);
        schedule.setDayType(DailyWorkSchedules.DayType.WORK);
        dailyWorkSchedulesRepository.save(schedule);
    }
}

