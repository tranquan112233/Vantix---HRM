package poly.edu.vantix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.PayrollAdjustRequest;
import poly.edu.vantix.dto.request.PayrollPeriodRequest;
import poly.edu.vantix.dto.response.PayrollPeriodResponse;
import poly.edu.vantix.dto.response.PayrollResponse;
import poly.edu.vantix.entity.Attendance;
import poly.edu.vantix.entity.Contract;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.LeaveRequest;
import poly.edu.vantix.entity.Payroll;
import poly.edu.vantix.entity.PayrollPeriod;
import poly.edu.vantix.entity.PublicHoliday;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.entity.enums.AttendanceStatus;
import poly.edu.vantix.entity.enums.EmploymentStatus;
import poly.edu.vantix.entity.enums.LeaveDayUnit;
import poly.edu.vantix.entity.enums.LeaveRequestStatus;
import poly.edu.vantix.entity.enums.LeaveType;
import poly.edu.vantix.entity.enums.PayrollStatus;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.repository.AttendanceRepository;
import poly.edu.vantix.repository.ContractRepository;
import poly.edu.vantix.repository.EmployeeRepository;
import poly.edu.vantix.repository.LeaveRequestRepository;
import poly.edu.vantix.repository.PayrollPeriodRepository;
import poly.edu.vantix.repository.PayrollRepository;
import poly.edu.vantix.repository.PublicHolidayRepository;
import poly.edu.vantix.repository.UserRepository;
import poly.edu.vantix.util.PayrollCalculation;
import poly.edu.vantix.util.PayrollInput;
import poly.edu.vantix.util.VietnamPayrollCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class PayrollService {

    private final PayrollPeriodRepository periodRepository;
    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final PayrollSettingService payrollSettingService;
    private final BusinessCalendarService businessCalendarService;
    private final PublicHolidayRepository publicHolidayRepository;

    public PayrollService(
            PayrollPeriodRepository periodRepository,
            PayrollRepository payrollRepository,
            EmployeeRepository employeeRepository,
            ContractRepository contractRepository,
            AttendanceRepository attendanceRepository,
            LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository,
            PayrollSettingService payrollSettingService,
            BusinessCalendarService businessCalendarService,
            PublicHolidayRepository publicHolidayRepository
    ) {
        this.periodRepository = periodRepository;
        this.payrollRepository = payrollRepository;
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.payrollSettingService = payrollSettingService;
        this.businessCalendarService = businessCalendarService;
        this.publicHolidayRepository = publicHolidayRepository;
    }

    // =============== PERIOD ===============

    @Transactional(readOnly = true)
    public List<PayrollPeriodResponse> listPeriods(Integer year) {
        return periodRepository.findAllByYear(year).stream()
                .map(PayrollPeriodResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PayrollPeriodResponse getPeriod(Long id) {
        return PayrollPeriodResponse.fromEntity(findActivePeriod(id));
    }

    @Transactional
    public PayrollPeriodResponse createPeriod(PayrollPeriodRequest request) {
        YearMonth ym = YearMonth.of(request.getYear(), request.getMonth());
        Optional<PayrollPeriod> existingPeriod = periodRepository.findByYearAndMonth(request.getYear(), request.getMonth());
        if (existingPeriod.isPresent() && !Boolean.TRUE.equals(existingPeriod.get().getDeleted())) {
            throw new BusinessException("month", "Payroll period for this month already exists");
        }

        PayrollPeriod period = existingPeriod.orElseGet(PayrollPeriod::new);
        period.setYear(request.getYear());
        period.setMonth(request.getMonth());
        period.setStartDate(request.getStartDate() != null ? request.getStartDate() : ym.atDay(1));
        period.setEndDate(request.getEndDate() != null ? request.getEndDate() : ym.atEndOfMonth());
        period.setStandardWorkDays(request.getStandardWorkDays() != null ? request.getStandardWorkDays() : 26);
        period.setNote(request.getNote());
        period.setStatus(PayrollStatus.DRAFT);
        period.setApprovedBy(null);
        period.setApprovedAt(null);
        period.setLockedAt(null);
        period.setDeleted(false);
        period.setDeletedAt(null);
        period.setDeletedBy(null);

        return PayrollPeriodResponse.fromEntity(periodRepository.save(period));
    }

    @Transactional
    public PayrollPeriodResponse updatePeriod(Long id, PayrollPeriodRequest request) {
        PayrollPeriod period = findActivePeriod(id);
        ensurePeriodEditable(period);

        period.setStandardWorkDays(request.getStandardWorkDays() != null ? request.getStandardWorkDays() : period.getStandardWorkDays());
        if (request.getStartDate() != null) period.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) period.setEndDate(request.getEndDate());
        period.setNote(request.getNote());

        return PayrollPeriodResponse.fromEntity(periodRepository.save(period));
    }

    @Transactional
    public void deletePeriod(Long id) {
        PayrollPeriod period = findActivePeriod(id);
        if (period.getStatus() == PayrollStatus.PAID) {
            throw new BusinessException("Cannot delete a paid period");
        }
        payrollRepository.findByPeriod(period.getId(), null, null)
                .forEach(payroll -> {
                    payroll.setDeleted(true);
                    payroll.setDeletedAt(LocalDateTime.now());
                });
        period.setDeleted(true);
        period.setDeletedAt(LocalDateTime.now());
        periodRepository.save(period);
    }

    // =============== PAYROLL ROWS ===============

    @Transactional(readOnly = true)
    public List<PayrollResponse> listPayrolls(Long periodId, String keyword, Long departmentId) {
        return payrollRepository.findByPeriod(periodId, keyword, departmentId).stream()
                .map(PayrollResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PayrollResponse getPayroll(Long id) {
        return PayrollResponse.fromEntity(findActivePayroll(id));
    }

    @Transactional(readOnly = true)
    public List<PayrollResponse> myPayrolls(Long userId) {
        Employee employee = employeeRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new BusinessException("No employee profile linked to your account"));
        return payrollRepository.findByEmployee(employee.getId()).stream()
                .map(PayrollResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PayrollResponse> getByEmployee(Long employeeId) {
        employeeRepository.findActiveById(employeeId)
                .orElseThrow(() -> new BusinessException("employeeId", "Employee does not exist"));
        return payrollRepository.findByEmployee(employeeId).stream()
                .map(PayrollResponse::fromEntity)
                .toList();
    }

    /**
     * Tạo dòng lương cho TẤT CẢ nhân viên đang làm việc trong kỳ. Không ghi đè nếu đã tồn tại.
     */
    @Transactional
    public PayrollPeriodResponse generate(Long periodId) {
        PayrollPeriod period = findActivePeriod(periodId);
        ensurePeriodEditable(period);

        LocalDate periodStart = period.getStartDate();
        LocalDate periodEnd = period.getEndDate();

        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
                .filter(e -> e.getStatus() == EmploymentStatus.ACTIVE
                        || e.getStatus() == EmploymentStatus.PROBATION)
                .filter(e -> e.getJoinDate() == null || !e.getJoinDate().isAfter(periodEnd))
                .filter(e -> e.getTerminationDate() == null || !e.getTerminationDate().isBefore(periodStart))
                .toList();

        for (Employee employee : activeEmployees) {
            Optional<Payroll> existing = payrollRepository
                    .findByPeriodIdAndEmployeeId(period.getId(), employee.getId());
            if (existing.isPresent() && !Boolean.TRUE.equals(existing.get().getDeleted())) {
                continue;
            }

            Payroll payroll = existing.orElseGet(Payroll::new);
            payroll.setPeriod(period);
            payroll.setEmployee(employee);
            payroll.setDeleted(false);
            payroll.setDeletedAt(null);
            payroll.setDeletedBy(null);
            payroll.setPaidAt(null);
            payroll.setStatus(PayrollStatus.DRAFT);
            applyContractDefaults(payroll, employee, period);
            autoFillTimesheet(payroll, employee, period);
            recalculate(payroll);
            payrollRepository.save(payroll);
        }

        List<Payroll> periodRows = payrollRepository.findByPeriod(period.getId(), null, null);
        period.setStatus(!periodRows.isEmpty()
                && periodRows.stream().allMatch(p -> p.getStatus() == PayrollStatus.CALCULATED)
                ? PayrollStatus.CALCULATED
                : PayrollStatus.DRAFT);
        return PayrollPeriodResponse.fromEntity(periodRepository.save(period));
    }

    /**
     * Đồng bộ lại chấm công/nghỉ phép đã duyệt rồi tính lại công thức cho tất cả dòng lương.
     */
    @Transactional
    public PayrollPeriodResponse recalculateAll(Long periodId) {
        PayrollPeriod period = findActivePeriod(periodId);
        ensurePeriodEditable(period);

        List<Payroll> payrolls = payrollRepository.findByPeriod(period.getId(), null, null);
        for (Payroll payroll : payrolls) {
            autoFillTimesheet(payroll, payroll.getEmployee(), period);
            recalculate(payroll);
            payrollRepository.save(payroll);
        }

        period.setStatus(PayrollStatus.CALCULATED);
        return PayrollPeriodResponse.fromEntity(periodRepository.save(period));
    }

    @Transactional
    public PayrollResponse adjustAndRecalculate(Long payrollId, PayrollAdjustRequest request) {
        throw new BusinessException("Manual payroll row adjustment is disabled. Use period recalculation instead");
    }

    @Transactional
    public PayrollPeriodResponse approve(Long periodId, Long approverUserId) {
        PayrollPeriod period = findActivePeriod(periodId);
        if (period.getStatus() == PayrollStatus.PAID) {
            throw new BusinessException("Period is already paid");
        }
        if (period.getStatus() != PayrollStatus.CALCULATED) {
            throw new BusinessException("Only CALCULATED periods can be approved");
        }
        if (payrollRepository.countByPeriodIdAndDeletedFalse(period.getId()) == 0) {
            throw new BusinessException("Period has no payroll rows to approve");
        }
        boolean hasUncalculatedRow = payrollRepository.findByPeriod(period.getId(), null, null)
                .stream()
                .anyMatch(p -> p.getStatus() != PayrollStatus.CALCULATED);
        if (hasUncalculatedRow) {
            throw new BusinessException("All payroll rows must be CALCULATED before approval");
        }

        User approver = userRepository.findById(approverUserId).orElse(null);
        period.setStatus(PayrollStatus.APPROVED);
        period.setApprovedBy(approver);
        period.setApprovedAt(LocalDateTime.now());

        payrollRepository.findByPeriod(period.getId(), null, null)
                .forEach(p -> p.setStatus(PayrollStatus.APPROVED));

        return PayrollPeriodResponse.fromEntity(periodRepository.save(period));
    }

    @Transactional
    public PayrollPeriodResponse markPaid(Long periodId) {
        PayrollPeriod period = findActivePeriod(periodId);
        if (period.getStatus() != PayrollStatus.APPROVED) {
            throw new BusinessException("Only APPROVED periods can be marked as paid");
        }

        LocalDateTime now = LocalDateTime.now();
        period.setStatus(PayrollStatus.PAID);
        period.setLockedAt(now);
        payrollRepository.findByPeriod(period.getId(), null, null)
                .forEach(p -> {
                    p.setStatus(PayrollStatus.PAID);
                    p.setPaidAt(now);
                });

        return PayrollPeriodResponse.fromEntity(periodRepository.save(period));
    }

    // =============== internal ===============

    private PayrollPeriod findActivePeriod(Long id) {
        return periodRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Payroll period not found with id: " + id));
    }

    private Payroll findActivePayroll(Long id) {
        return payrollRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Payroll not found with id: " + id));
    }

    private void ensurePeriodEditable(PayrollPeriod period) {
        if (period.getStatus() == PayrollStatus.APPROVED || period.getStatus() == PayrollStatus.PAID) {
            throw new BusinessException("Cannot modify an approved or paid period");
        }
        if (period.getStatus() == PayrollStatus.CANCELLED) {
            throw new BusinessException("Cannot modify a cancelled period");
        }
    }

    private void applyContractDefaults(Payroll payroll, Employee employee, PayrollPeriod period) {
        List<Contract> effective = contractRepository
                .findEffectiveContracts(employee.getId(), period.getEndDate());
        Contract contract = effective.isEmpty() ? null : effective.get(0);

        payroll.setContract(contract);
        payroll.setStandardWorkDays(period.getStandardWorkDays());

        if (contract != null) {
            payroll.setBaseSalary(nz(contract.getBaseSalary()));
            payroll.setInsuranceSalary(
                    contract.getInsuranceSalary() != null
                            ? contract.getInsuranceSalary()
                            : nz(contract.getBaseSalary())
            );
            payroll.setResponsibilityAllowance(nz(contract.getResponsibilityAllowance()));
            payroll.setMealAllowance(nz(contract.getMealAllowance()));
            payroll.setTransportAllowance(nz(contract.getTransportAllowance()));
            payroll.setPhoneAllowance(nz(contract.getPhoneAllowance()));
            payroll.setOtherAllowance(nz(contract.getOtherAllowance()));
            if (contract.getStandardWorkDays() != null && contract.getStandardWorkDays() > 0) {
                payroll.setStandardWorkDays(contract.getStandardWorkDays());
            }
        }
    }

    private void autoFillTimesheet(Payroll payroll, Employee employee, PayrollPeriod period) {
        LocalDate from = period.getStartDate();
        LocalDate to = period.getEndDate();

        // Đếm ngày nghỉ đã duyệt trong kỳ. Ngày nghỉ không lương không được cộng vào lương ngày công.
        List<LeaveRequest> leaves = leaveRequestRepository
                .search(employee.getId(), null, LeaveRequestStatus.APPROVED, null, from, to);

        BigDecimal paidLeave = BigDecimal.ZERO;
        BigDecimal unpaidLeave = BigDecimal.ZERO;
        Map<LocalDate, BigDecimal> approvedLeaveByDate = new HashMap<>();

        for (LeaveRequest lr : leaves) {
            LocalDate start = lr.getStartDate().isBefore(from) ? from : lr.getStartDate();
            LocalDate end = lr.getEndDate().isAfter(to) ? to : lr.getEndDate();
            if (end.isBefore(start)) continue;

            BigDecimal leaveDays = businessCalendarService.countWorkingLeaveDays(
                    employee.getId(),
                    start,
                    end,
                    lr.getDayUnit() == null ? LeaveDayUnit.FULL : lr.getDayUnit()
            );

            if (lr.getType() == LeaveType.UNPAID) {
                unpaidLeave = unpaidLeave.add(leaveDays);
            } else if (lr.getType() == LeaveType.SICK_OR_MATERNITY) {
                // Nghỉ ốm/thai sản do BHXH chi trả, không tính lương công ty
                unpaidLeave = unpaidLeave.add(leaveDays);
            } else {
                paidLeave = paidLeave.add(leaveDays);
            }

            BigDecimal leaveUnit = lr.getDayUnit() == LeaveDayUnit.HALF
                    ? new BigDecimal("0.5")
                    : BigDecimal.ONE;
            LocalDate cursor = start;
            while (!cursor.isAfter(end)) {
                if (businessCalendarService.isWorkingDate(employee.getId(), cursor)) {
                    approvedLeaveByDate.merge(cursor, leaveUnit, BigDecimal::add);
                }
                cursor = cursor.plusDays(1);
            }
        }

        // Đếm ngày công thực tế từ attendance, trừ phần ngày đã có đơn nghỉ để tránh trả trùng hoặc không trừ lương.
        List<Attendance> attendances = attendanceRepository.search(from, to, employee.getId());
        Set<LocalDate> payableAttendanceDates = new HashSet<>();
        BigDecimal workedDays = attendances.stream()
                .filter(a -> a.getStatus() != AttendanceStatus.ABSENT
                        && a.getStatus() != AttendanceStatus.PENDING
                        && a.getStatus() != AttendanceStatus.MISSING_CHECKOUT
                        && a.getCheckInAt() != null
                        && a.getCheckOutAt() != null)
                .map(a -> {
                    payableAttendanceDates.add(a.getWorkDate());
                    return BigDecimal.ONE.subtract(
                            approvedLeaveByDate.getOrDefault(a.getWorkDate(), BigDecimal.ZERO).min(BigDecimal.ONE)
                    ).max(BigDecimal.ZERO);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidPublicHolidayDays = publicHolidayRepository.findByHolidayDateBetweenAndDeletedFalse(from, to).stream()
                .filter(holiday -> Boolean.TRUE.equals(holiday.getPaidDay()))
                .map(PublicHoliday::getHolidayDate)
                .filter(date -> businessCalendarService.isScheduledWorkingDate(employee.getId(), date))
                .filter(date -> !payableAttendanceDates.contains(date))
                .map(date -> BigDecimal.ONE.subtract(
                        approvedLeaveByDate.getOrDefault(date, BigDecimal.ZERO).min(BigDecimal.ONE)
                ).max(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        payroll.setActualWorkDays(workedDays);
        payroll.setPaidLeaveDays(paidLeave.add(paidPublicHolidayDays));
        payroll.setUnpaidLeaveDays(unpaidLeave);
    }

    private void recalculate(Payroll payroll) {
        PayrollInput input = PayrollInput.builder()
                .baseSalary(payroll.getBaseSalary())
                .insuranceSalary(payroll.getInsuranceSalary())
                .standardWorkDays(payroll.getStandardWorkDays() != null ? payroll.getStandardWorkDays() : 26)
                .hoursPerDay(payroll.getContract() != null ? payroll.getContract().getHoursPerDay() : null)
                .actualWorkDays(payroll.getActualWorkDays())
                .paidLeaveDays(payroll.getPaidLeaveDays())
                .unpaidLeaveDays(payroll.getUnpaidLeaveDays())
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
                .policy(payrollSettingService.getPolicy())
                .build();

        PayrollCalculation result = VietnamPayrollCalculator.calculate(input);

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

        if (payroll.getStatus() == null || payroll.getStatus() == PayrollStatus.DRAFT) {
            payroll.setStatus(PayrollStatus.CALCULATED);
        }
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
