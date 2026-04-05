package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.salaries.ResponseDepartmentDTO;
import poly.edu.vantix_hrm.dto.salaries.ResponseSalaryTableDTO;
import poly.edu.vantix_hrm.entity.*;
import poly.edu.vantix_hrm.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalariesService {

    private final DepartmentRepository departmentRepository;
    private final SalariesRepository salariesRepository;
    private final MonthlySchedulesRepository monthlySchedulesRepository;
    private final AttendanceRepository attendanceRepository;
    private final ContractsRepository contractRepository;
    private final ContractAnnexesRepository contractAnnexesRepository;


    /**
     * Lấy danh sáchtên tất cả các phòng ban chưa bị xóa.
     *
     * @return Danh sách ResponseDepartmentDTO chứa tên phòng ban.
     */
    public List<ResponseDepartmentDTO> findDepartmentNames() {
        List<Department> departments = departmentRepository.findAll();
        if (departments.isEmpty()) {
            throw new RuntimeException("Không tìm dữ liệu Phòng Ban nào!");
        }
        return departments.stream().map(dept -> ResponseDepartmentDTO.builder().departmentName(dept.getName()).build()).toList();
    }


    /**
     * Lấy danh sách bảng lương theo tháng và năm, đồng thời map sang DTO.
     */
    public List<ResponseSalaryTableDTO> getSalariesByMonthAndYear(int month, int year) {
        // Lấy danh sách Salary từ database bằng hàm JPQL đã tối ưu (JOIN FETCH)
        List<Salary> salaries = salariesRepository.findByMonthAndYear(month, year);

        // Map từng phần tử Entity sang DTO và trả về list
        return salaries.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Hàm helper để thực hiện map từ Entity Salary sang DTO ResponseSalaryTableDTO
     */
    private ResponseSalaryTableDTO mapToDTO(Salary salary) {
        return ResponseSalaryTableDTO.builder().id(salary.getSalaryId()).employeeId(salary.getEmployee() != null ? salary.getEmployee().getId() : null).employeeName(salary.getEmployee() != null ? salary.getEmployee().getFullName() : null).department(salary.getEmployee() != null && salary.getEmployee().getDepartment() != null ? salary.getEmployee().getDepartment().getName() : null).salaryMonth(salary.getSalaryMonth()).baseSalarySnapshot(salary.getBaseSalarySnapshot()).standardWorkDays(salary.getStandardWorkDays())
                // actualWorkDays nay đã là Integer ở cả 2 bên nên truyền thẳng
                .actualWorkDays(salary.getActualWorkDays()).allowance(salary.getAllowance()).bonus(salary.getBonus()).bhxhAmount(salary.getBhxhAmount()).bhytAmount(salary.getBhytAmount()).bhtnAmount(salary.getBhtnAmount()).taxAmount(salary.getTaxAmount()).totalIncome(salary.getTotalIncome()).totalDeduction(salary.getTotalDeduction()).netSalary(salary.getNetSalary()).status(salary.getStatus() != null ? salary.getStatus().name() : null).note(salary.getNote()).build();
    }

    /**
     * Chuyển trạng thái toàn bộ bảng lương của một tháng sang PENDING.
     * * @return Số lượng bản ghi đã được cập nhật
     */
    public int submitAllSalariesToPending(int month, int year) {
        // Truyền thẳng Enum PENDING vào câu Query
        int updatedCount = salariesRepository.updateStatusByMonthAndYear(Salary.SalaryStatus.PENDING, month, year);
        if (updatedCount == 0) {
            throw new RuntimeException("Không có bản ghi bảng lương nào trong tháng " + month + "/" + year + " để cập nhật!");
        }
        return updatedCount;
    }

    /**
     * API Cốt lõi: Tự động tính toán bảng lương cho toàn bộ nhân viên trong tháng
     */
    public String generateSalaries(int month, int year) {
        // 1. Lấy danh sách lịch làm việc của tháng
        List<MonthlySchedules> schedules = monthlySchedulesRepository.findByMonthAndYear(month, year);
        if (schedules.isEmpty()) {
            throw new RuntimeException("Chưa có lịch làm việc nào được xếp trong tháng " + month + "/" + year);
        }

        // 2. Tính số ngày công chuẩn (Tổng ngày trong tháng - Số ngày Chủ Nhật)
        int standardWorkDays = calculateStandardWorkDays(month, year);
        int generatedCount = 0;

        for (MonthlySchedules schedule : schedules) {
            Employee emp = schedule.getEmployee();

            // 3. Lấy Lương cơ bản (Base Salary) từ Hợp đồng hoặc Phụ lục mới nhất
            BigDecimal baseSalary = getBaseSalary(emp, month, year);
            if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) == 0) continue;

            // 4. Quét bảng Chấm công (Attendance)
            List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndMonthAndYear(emp.getId(), month, year);
            int actualWorkDays = 0;
            int totalPenaltyMinutes = 0;

            for (Attendance att : attendances) {
                // Có lịch + Có check in/out = 1 công
                if (att.getCheckIn() != null && att.getCheckOut() != null) {
                    actualWorkDays++;
                }
                // Cộng dồn số phút đi muộn / về sớm
                totalPenaltyMinutes += (att.getLateMinutes() != null ? att.getLateMinutes() : 0);
                totalPenaltyMinutes += (att.getEarlyLeaveMinutes() != null ? att.getEarlyLeaveMinutes() : 0);
            }

            // 5. Tính Thu nhập
            BigDecimal actualWorkDaysBD = BigDecimal.valueOf(actualWorkDays);
            BigDecimal standardWorkDaysBD = BigDecimal.valueOf(standardWorkDays);

            // Total Income = (Base / Standard) * Actual + 0 + 0
            BigDecimal totalIncome = baseSalary.divide(standardWorkDaysBD, 2, RoundingMode.HALF_UP)
                    .multiply(actualWorkDaysBD);

            // 6. Tính Phạt (Làm tròn: Dưới 30 phút -> 0, Trên hoặc bằng 30 -> 1 tiếng)
            long penaltyHours = Math.round((double) totalPenaltyMinutes / 60);
            BigDecimal totalDeduction = BigDecimal.valueOf(penaltyHours * 50000);

            // 7. Tính Bảo hiểm (Dựa trên Base Salary Snapshot)
            BigDecimal bhxh = baseSalary.multiply(BigDecimal.valueOf(0.08));
            BigDecimal bhyt = baseSalary.multiply(BigDecimal.valueOf(0.015));
            BigDecimal bhtn = baseSalary.multiply(BigDecimal.valueOf(0.01));
            BigDecimal totalInsurance = bhxh.add(bhyt).add(bhtn);

            // 8. Tính Thuế TNCN
            // Thu nhập tính thuế = Tổng thu nhập - Bảo hiểm - Giảm trừ bản thân (11 triệu)
            BigDecimal taxableIncome = totalIncome.subtract(totalInsurance).subtract(BigDecimal.valueOf(11000000));
            BigDecimal taxAmount = calculatePIT(taxableIncome);

            // 9. Lương thực nhận (Net Salary)
            BigDecimal netSalary = totalIncome.subtract(totalDeduction).subtract(totalInsurance).subtract(taxAmount);

            // 10. Lưu xuống DB
            Optional<Salary> existingSalaryOpt = salariesRepository.findByEmployeeAndMonthAndYear(emp.getId(), month, year);
            Salary salary = existingSalaryOpt.orElse(new Salary());

            salary.setEmployee(emp);
            salary.setSalaryMonth(LocalDate.of(year, month, 1));
            salary.setBaseSalarySnapshot(baseSalary);
            salary.setStandardWorkDays(standardWorkDays);
            salary.setActualWorkDays(actualWorkDays);
            salary.setAllowance(BigDecimal.ZERO);
            salary.setBonus(BigDecimal.ZERO);
            salary.setBhxhAmount(bhxh);
            salary.setBhytAmount(bhyt);
            salary.setBhtnAmount(bhtn);
            salary.setTaxAmount(taxAmount);
            salary.setTotalIncome(totalIncome);
            salary.setTotalDeduction(totalDeduction);
            salary.setNetSalary(netSalary);
            salary.setStatus(Salary.SalaryStatus.DRAFT);

            salariesRepository.save(salary);
            generatedCount++;
        }

        return "Thành công! Đã tính toán và tạo " + generatedCount + " bảng lương nháp.";
    }

    // --- CÁC HÀM HELPER NỘI BỘ ---

    // Hàm đếm số ngày công chuẩn (Trừ Chủ Nhật)
    private int calculateStandardWorkDays(int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        int sundays = 0;
        for (int i = 1; i <= daysInMonth; i++) {
            if (yearMonth.atDay(i).getDayOfWeek() == DayOfWeek.SUNDAY) {
                sundays++;
            }
        }
        return daysInMonth - sundays;
    }

    // Hàm lấy Lương cơ bản hiện hành
    private BigDecimal getBaseSalary(Employee emp, int month, int year) {
        Optional<Contract> activeContractOpt = contractRepository.findActiveContractByEmployeeId(emp.getId());
        if (activeContractOpt.isEmpty()) return null;

        Contract contract = activeContractOpt.get();
        BigDecimal baseSalary = contract.getBaseSalary();

        // Kiểm tra phụ lục hợp đồng có hiệu lực trước hoặc trong tháng này
        LocalDate targetDate = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth());
        List<ContractAnnexes> validAnnexes = contractAnnexesRepository.findValidAnnexes(contract.getContractId(), targetDate);

        if (!validAnnexes.isEmpty() && validAnnexes.get(0).getNewSalary() != null) {
            baseSalary = validAnnexes.get(0).getNewSalary();
        }
        return baseSalary;
    }

    // Hàm tính Thuế TNCN (Biểu thuế Lũy tiến 7 bậc tiêu chuẩn của VN)
    private BigDecimal calculatePIT(BigDecimal taxableIncome) {
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO; // Không phải nộp thuế
        }
        double income = taxableIncome.doubleValue();
        double tax = 0;

        if (income <= 5_000_000) {
            tax = income * 0.05;
        } else if (income <= 10_000_000) {
            tax = income * 0.10 - 250_000;
        } else if (income <= 18_000_000) {
            tax = income * 0.15 - 750_000;
        } else if (income <= 32_000_000) {
            tax = income * 0.20 - 1_650_000;
        } else if (income <= 52_000_000) {
            tax = income * 0.25 - 3_250_000;
        } else if (income <= 80_000_000) {
            tax = income * 0.30 - 5_850_000;
        } else {
            tax = income * 0.35 - 9_850_000;
        }

        return BigDecimal.valueOf(tax);
    }
}